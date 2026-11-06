// app.js: wiring. It reads config, holds page state, and connects buttons to features.
//
// This is the INSECURE starting point. Every part of the code-along changes something here.
// Look for the "TODO Part N" markers.

import * as api from "./api.js";
import { readDraft, validateDraft } from "./form.js";
import {
  renderEventList, renderPreview, renderDetail, renderFeatured,
  renderBanner, renderGreeting, showMessage, showErrors,
} from "./render.js";
import {
  parseUntrusted, safeMerge, isPlainObject, validate, validateList, withoutSecrets,
  EVENT_SCHEMA, EVENT_DETAIL_SCHEMA, SESSION_SCHEMA, FILTERS_SCHEMA, ATTENDANCE_SCHEMA,
} from "./safe-json.js";

const $ = (id) => document.getElementById(id);

// TODONE Part 1: read config from a <script type="application/json"> block instead of
// a global set by an inline script.

function readConfig() {
    const defaults = { apiBase: "/api", timeoutMs: 5000, partnerFeed: ""};
    let c; //Im a bad variable name
    try {
      c = JSON.parse(document.getElementById("app-config").textContent);
    } catch {
      return defaults;
    }
    if (typeof c !== "object" || c === null){
      return defaults;
    }
    return {
      apiBase: typeof c.apiBase === "string" ? c.apiBase : defaults.apiBase,
      timeoutMs: Number.isInteger(c.timeoutMs) && c.timeoutMs > 0 ? c.timeoutMs : defaults.timeoutMs,
      partnerFeed: typeof c.partnerFeed === "string" ? c.partnerFeed : defaults.partnerFeed,
    };
}

const config = readConfig();
const status = $("status");

// ---------------------------------------------------------------------------
// Saved preferences and session ("so you don't have to sign in every time")
// TODO Part 3: the token must not live in localStorage.
// TODO Part 4: saved filters are untrusted text. Parse, merge and validate them safely.
// ---------------------------------------------------------------------------
const STORAGE_KEY = "neighborly.prefs";
// const SESSION_KEY = "neighborly.session";

function deepMerge(target, source) {
  for (const key in source) {
    if (typeof source[key] === "object" && source[key] !== null) {
      if (typeof target[key] !== "object" || target[key] === null) target[key] = {};
      deepMerge(target[key], source[key]);
    } else {
      target[key] = source[key];
    }
  }
  return target;
}

function loadSavedFilters() {
  const filters = { category: "all", sortBy: "soonest" };
  const raw = localStorage.getItem(STORAGE_KEY);
  if (raw) deepMerge(filters, JSON.parse(raw).filters);
  return filters;
}

const state = {
  user: null,
  //token: null,
  events: [],
  filters: loadSavedFilters(),
};

// ---------------------------------------------------------------------------
// Rendering helpers
// ---------------------------------------------------------------------------
function visibleEvents() {
  const { category, sortBy } = state.filters;
  const list = state.events.filter((e) => category === "all" || e.category === category);
  const dir = sortBy === "latest" ? -1 : 1;
  return list.sort((a, b) => dir * (new Date(a.date) - new Date(b.date)));
}

function renderList() {
  renderEventList($("event-list"), visibleEvents());
}

function syncFilterControls() {
  $("filter-category").value = state.filters.category;
  $("filter-sort").value = state.filters.sortBy;
}

function showSignedIn() {
  renderGreeting($("user-greeting"), state.user);
  $("user-greeting").hidden = false;
  $("sign-in-form").hidden = true;
  $("sign-out-btn").hidden = false;
  $("expire-btn").hidden = false;
  $("organizer-tools").hidden = !state.user.isOrganizer;
}


function showSignedOut(message) {
  state.user = null;
  // state.token = null;
  state.events = [];
  $("user-greeting").hidden = true;
  $("sign-in-form").hidden = false;
  $("sign-out-btn").hidden = true;
  $("expire-btn").hidden = true;
  $("organizer-tools").hidden = true;
  $("edit-event").hidden = true;
  $("event-list").replaceChildren();
  $("event-detail").replaceChildren();
  $("featured").replaceChildren();
  if (message) showMessage(status, message, "error");
}

api.configure({
  apiBase: config.apiBase,
  timeoutMs: config.timeoutMs,
  onUnauthorized: ()=> showSignedOut("Your session has ended. PLease sign in again.")
});

function report(err){
  showMessage(status, err instanceof api.ApiError ? err.message: "Somethingf went wrong.","error");
  if(!(err instanceof api.ApiError)) console.error(err);
}
// ---------------------------------------------------------------------------
// Features
// TODO Part 3: eight separate fetch() calls, each with its own (missing) checks.
// Route them all through apiRequest() in api.js.
// ---------------------------------------------------------------------------
const USERNAME_PATTERN = /^[A-Za-z0-9_-]{3,20}$/;

function readCredentials() {
  const username = $("sign-in-username").value.trim();
  const password = $("sign-in-password").value;
  if (!USERNAME_PATTERN.test(username) || password.length < 8 || password.length > 72) {
    showMessage(status, "Username: 3–20 letters, digits, _ or -. Password: 8–72 characters.", "error");
    return null;
  }
  return { username, password };
}

async function signIn(event) {
  event.preventDefault();
  const credentials = readCredentials();
  if (!credentials) return;
  try {
    const session = await api.apiRequest("/session", {
      method: "POST",
      body: credentials, // in the POST body, never in a URL
      validate: (v) => validate(SESSION_SCHEMA, v),
    });
    $("sign-in-password").value = ""; // don't leave the password sitting in the page
    api.setToken(session.token); // memory only
    state.user = session.user;
    showSignedIn();
    showMessage(status, "Signed in.");
    await loadEvents();
  } catch (err) {
    // One message for "no such user" and "wrong password", so sign-in can't be used to discover usernames.
    if (err instanceof api.ApiError && err.status === 401) showMessage(status, "Username or password is incorrect.", "error");
    else report(err);
  }
}

async function createAccount(event) {
  event.preventDefault();
  const credentials = readCredentials();
  if (!credentials) return;
  try {
    await api.apiRequest("/accounts", { method: "POST", body: credentials }); // we don't use the response body
  } catch (err) {
    if (err instanceof api.ApiError && err.status === 409) showMessage(status, "That username is taken.", "error");
    else report(err);
    return;
  }
  await signIn(event);
}

async function loadEvents() {
  if (!api.isSignedIn()) {
    showMessage(status, "Sign in to see events.");
    return;
  }
  try {
    state.events = await api.apiRequest("/events", { validate: (v) => validateList(EVENT_SCHEMA, v) });
    renderList();
    showMessage(status, `Loaded ${state.events.length} events.`);
  } catch (err) {
    report(err);
  }
}

async function showDetails(id) {
  $("edit-event").hidden = true;
  try {
    const detail = await api.apiRequest(`/events/${id}`, { validate: (v) => validate(EVENT_DETAIL_SCHEMA, v) });
    renderDetail($("event-detail"), detail);
    $("edit-event").hidden = !detail.canEdit; // TODO Part 4
    showMessage(status, "");
  } catch (err) {
    $("event-detail").replaceChildren();
    report(err);
  }
}

async function loadFeatured() {
  try {
    const ev = await api.apiRequest("/events/featured", { validate: (v) => validate(EVENT_SCHEMA, v) });
    renderFeatured($("featured"), ev);
  } catch (err) {
    $("featured").replaceChildren();
    report(err);
  }
}

async function refreshAttendance() {
  showMessage(status, "Checking attendance…");
  try {
    const a = await api.apiRequest("/attendance", { validate: (v) => validate(ATTENDANCE_SCHEMA, v) });
    showMessage(status, `${a.checkedIn} people checked in.`);
  } catch (err) {
    report(err);
  }
}

async function loadPartnerEvents() {
  // Partner data is a different origin. It isn't in connect-src and their server
  // sends no CORS grant, so the wrapper refuses it before any request is made.
  try {
    await api.apiRequest(config.partnerFeed);
  } catch (err) {
    report(err);
  }
}

async function expireSession() {
  try {
    await api.apiRequest("/session/expire", { method: "POST" });
    showMessage(status, "The server dropped your session. Click \"Refresh events\" to see what happens.");
  } catch (err) {
    report(err);
  }
}

function signOut() {
  api.clearToken();
  showSignedOut();
  showMessage(status, "Signed out.");
}

function openOrganizerTools() {
  showMessage(status, "Organizer tools opened.");
}

function previewDraft() {
  const result = validateDraft(readDraft($("post-form")));
  showErrors($("form-errors"), result.ok ? [] : result.errors);
  if (result.ok) renderPreview($("preview"), result.value);
  else $("preview").replaceChildren();
  return result;
}

async function postEvent(event) {
  event.preventDefault();
  const result = previewDraft();
  if (!result.ok) return;
  if (!api.isSignedIn()) {
    showMessage(status, "Sign in to post an event.", "error");
    return;
  }
  try {
    const created = await api.apiRequest("/events", {
      method: "POST",
      body: result.value,
      validate: (v) => validate(EVENT_SCHEMA, v),
    });
    state.events.push(created);
    renderList();
    showMessage(status, "Event posted.");
  } catch (err) {
    report(err);
  }
}

function importFilters() {
  const incoming = JSON.parse($("filters-json").value);
  deepMerge(state.filters, incoming);
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ filters: incoming }));
  syncFilterControls();
  renderList();
  if (state.user) showSignedIn();
  showMessage(status, "Filters imported.");
}

function changeFilter() {
  state.filters.category = $("filter-category").value;
  state.filters.sortBy = $("filter-sort").value;
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ filters: state.filters }));
  renderList();
}

function resetDemo() {
  localStorage.removeItem(STORAGE_KEY);
  localStorage.removeItem("neighborly.session");
  location.assign("/");
}



// ---------------------------------------------------------------------------
// PART 1: event wiring (replaces every inline onclick/onsubmit/onchange)
// ---------------------------------------------------------------------------
$("sign-in-form").addEventListener("submit", signIn);
$("create-account-btn").addEventListener("click", createAccount);
$("sign-out-btn").addEventListener("click", signOut);
$("expire-btn").addEventListener("click", expireSession);
$("organizer-tools").addEventListener("click", openOrganizerTools);
$("refresh-btn").addEventListener("click", loadEvents);
$("featured-btn").addEventListener("click", loadFeatured);
$("attendance-btn").addEventListener("click", refreshAttendance);
$("partner-btn").addEventListener("click", loadPartnerEvents);
$("preview-btn").addEventListener("click", previewDraft);
$("post-form").addEventListener("submit", postEvent);
$("import-filters-btn").addEventListener("click", importFilters);
$("reset-demo-btn").addEventListener("click", resetDemo);
$("filter-category").addEventListener("change", changeFilter);
$("filter-sort").addEventListener("change", changeFilter);




// ---------------------------------------------------------------------------
// Start-up
// ---------------------------------------------------------------------------
syncFilterControls();
const welcome = new URLSearchParams(location.search).get("welcome");
if (welcome) renderBanner($("welcome-banner"), welcome.trim().slice(0, 60));
showMessage(status, "Sign in to see events.");

//DELETE ME
// const saved = localStorage.getItem(SESSION_KEY);
// if (saved) {
//   const session = JSON.parse(saved);
//   state.token = session.token;
//   state.user = session.user;
//   showSignedIn();
//   loadEvents();
// } else {
//   showMessage(status, "Sign in to see events.");
// }
//END DELETE