// app.js: wiring. It reads config, holds page state, and connects buttons to features.
//
// This is the INSECURE starting point. Every part of the code-along changes something here.
// Look for the "TODO Part N" markers.

import { readDraft, validateDraft } from "./form.js";
import {
  renderEventList, renderPreview, renderDetail, renderFeatured,
  renderBanner, renderGreeting, showMessage, showErrors,
} from "./render.js";

const $ = (id) => document.getElementById(id);

// TODO Part 1: read config from a <script type="application/json"> block instead of
// a global set by an inline script.
const config = window.NEIGHBORLY_CONFIG;
const status = $("status");

// ---------------------------------------------------------------------------
// Saved preferences and session ("so you don't have to sign in every time")
// TODO Part 3: the token must not live in localStorage.
// TODO Part 4: saved filters are untrusted text. Parse, merge and validate them safely.
// ---------------------------------------------------------------------------
const STORAGE_KEY = "neighborly.prefs";
const SESSION_KEY = "neighborly.session";

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
  token: null,
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
  state.token = null;
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

// ---------------------------------------------------------------------------
// Features
// TODO Part 3: eight separate fetch() calls, each with its own (missing) checks.
// Route them all through apiRequest() in api.js.
// ---------------------------------------------------------------------------
async function signIn(event) {
  event.preventDefault();
  const username = $("sign-in-username").value.trim();
  const password = $("sign-in-password").value;
  const res = await fetch(`${config.apiBase}/session`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const session = await res.json();
  if (!res.ok) {
    showMessage(status, `Error ${res.status}: ${JSON.stringify(session)}`, "error");
    return;
  }
  console.log("signed in", session);
  state.token = session.token;
  state.user = session.user;
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  showSignedIn();
  await loadEvents();
}

async function createAccount(event) {
  event.preventDefault();
  const username = $("sign-in-username").value.trim();
  const password = $("sign-in-password").value;
  const res = await fetch(`${config.apiBase}/accounts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const data = await res.json();
  if (!res.ok) {
    showMessage(status, `Error ${res.status}: ${JSON.stringify(data)}`, "error");
    return;
  }
  await signIn(event);
}

async function loadEvents() {
  const res = await fetch(`${config.apiBase}/events?access_token=${state.token}`);
  const data = await res.json();
  if (!res.ok) {
    showMessage(status, `Error ${res.status}: ${JSON.stringify(data)}`, "error");
    return;
  }
  state.events = data;
  renderList();
  showMessage(status, `Loaded ${state.events.length} events.`);
}

async function showDetails(id) {
  const res = await fetch(`${config.apiBase}/events/${id}?access_token=${state.token}`);
  const detail = await res.json();
  const ev = Object.assign({}, state.events.find((e) => e.id === id), detail);
  renderDetail($("event-detail"), ev);
  $("edit-event").hidden = !ev.canEdit;
}

async function loadFeatured() {
  const res = await fetch(`${config.apiBase}/events/featured?access_token=${state.token}`);
  const text = await res.text();
  try {
    renderFeatured($("featured"), JSON.parse(text));
  } catch {
    // "Helpful" fallback: show whatever the server sent.
    $("featured").innerHTML = text;
  }
}

async function refreshAttendance() {
  showMessage(status, "Checking attendance…");
  const res = await fetch(`${config.apiBase}/attendance?access_token=${state.token}`);
  const data = await res.json();
  showMessage(status, `${data.checkedIn} people checked in.`);
}

async function loadPartnerEvents() {
  try {
    const res = await fetch(config.partnerFeed);
    const data = await res.json();
    showMessage(status, `Partner sent ${data.length} events.`);
  } catch (err) {
    showMessage(status, `Partner feed failed: ${err.message}`, "error");
  }
}

async function expireSession() {
  await fetch(`${config.apiBase}/session/expire?access_token=${state.token}`, { method: "POST" });
  showMessage(status, "The server dropped your session. Click \"Refresh events\" to see what happens.");
}

function signOut() {
  localStorage.removeItem(SESSION_KEY);
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
  const res = await fetch(`${config.apiBase}/events?access_token=${state.token}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(result.value),
  });
  const data = await res.json();
  if (!res.ok) {
    showMessage(status, `Error ${res.status}: ${JSON.stringify(data)}`, "error");
    return;
  }
  state.events.push(data);
  renderList();
  showMessage(status, "Event posted.");
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
  localStorage.removeItem(SESSION_KEY);
  location.assign("/");
}

// ---------------------------------------------------------------------------
// TODO Part 1: the inline onclick/onsubmit/onchange attributes in index.html
// need these globals. Replace them with addEventListener calls here.
// ---------------------------------------------------------------------------
Object.assign(window, {
  signIn, createAccount, signOut, expireSession, openOrganizerTools, loadEvents, loadFeatured,
  refreshAttendance, loadPartnerEvents, previewDraft, postEvent, importFilters,
  changeFilter, resetDemo,
});

$("event-list").addEventListener("click", (e) => {
  const btn = e.target instanceof Element ? e.target.closest("button[data-event-id]") : null;
  if (btn) showDetails(Number(btn.dataset.eventId));
});

// ---------------------------------------------------------------------------
// Start-up
// ---------------------------------------------------------------------------
syncFilterControls();
const welcome = new URLSearchParams(location.search).get("welcome");
if (welcome) renderBanner($("welcome-banner"), welcome);

const saved = localStorage.getItem(SESSION_KEY);
if (saved) {
  const session = JSON.parse(saved);
  state.token = session.token;
  state.user = session.user;
  showSignedIn();
  loadEvents();
} else {
  showMessage(status, "Sign in to see events.");
}
