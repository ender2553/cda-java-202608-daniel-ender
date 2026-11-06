# Code-Along Review: Harden the Neighborly Event Board

**Course:** Web Development with JavaScript — Secure-by-Design
**Reviews:** Module 1 (Content Security Policy), Module 2 (Input Sanitization), Module 3 (Secure Data Fetching), Module 4 (Secure JSON Parsing)

## What we'll build

Neighborly is a small community event board. It works, and it is insecure in every layer you studied:

- Event data from the API becomes live HTML on the page.
- A `javascript:` link sits in the list, waiting for a click.
- The session token is kept in `localStorage` and sent in URLs.
- A broken server response gets pasted straight into the page.
- One pasted JSON snippet can give every object in the app an `isOrganizer` flag.

You'll fix it in four parts, one per module. Each part adds a layer, and the **Break-it board** on the page shows the effect as you go. When you finish, every attack is blocked, and each fix sits in one clear place in the code.

| Part | Layer | Lessons | You edit |
|---|---|---|---|
| 0 | Recon: break it first | — | nothing |
| 1 | Content Security Policy | 1–5 | `headers.json`, `public/index.html`, `public/app.js` |
| 2 | Sanitize and render safely | 6–10 | `public/render.js`, `public/form.js`, `public/app.js` |
| 3 | Secure fetch | 11–15 | `public/api.js`, `public/app.js` |
| 4 | Secure JSON | 16–19 | `public/safe-json.js`, `public/app.js` |

You never need to edit `server.js`, `breakit.js`, `styles.css`, or `vendor/purify.min.js`.

Some long blocks are marked **(copy-paste)**. Copy those straight from this guide so class time goes to the ideas, not the typing. Type everything else yourself.

## Setup

1. Install **Node.js 18 or newer** (`node --version` to check). You don't need `npm install`.
2. In a terminal, from this folder, run:

   ```
   node server.js
   ```

   Leave this terminal open where you can see it. It's your **server log**. Every request, and every CSP report, prints here.
3. Open **http://localhost:8080** in Chrome or Edge. Open DevTools (F12) and keep the **Console** tab visible.

> **Every "attack" in this app is local and harmless.** A successful payload calls `__breakit("...")`. That only flips a row on the Break-it board and logs a red `[break-it]` line in the console. Nothing leaves your machine.

**How to read the Break-it board**

| Status | Meaning |
|---|---|
| **FIRED** | The payload's JavaScript actually ran. |
| **LIVE** | The dangerous markup or data is in the page, even if it hasn't run yet. |
| **blocked** | Your defense is working. |
| not tried | You haven't used that feature yet on this page load. |

A row that went LIVE or FIRED stays that way until you **reload**. So after each fix, reload the page and try the attack again.

---

## Part 0: Recon (break it first)

Before you fix anything, see what's wrong. Do each step and watch the board.

1. Type a username (3–20 letters, digits, `_` or `-`) and a password (8+ characters), then click **Create account**. You're signed in, and event titles and descriptions **fire** right away. (The bad link waits for a click: step 3.) Next time, use **Sign in** with the same details. Accounts last until the server restarts.
2. Click **Try the URL payload** under the board. The welcome banner fires.
3. In the "Free Pizza Night" card, click **Event website**. It fires.
4. Click **Load featured**. The server sent HTML instead of JSON, and the page rendered it.
5. Click **Details** on the pottery event. An **Edit this event** button appears. You're not the organizer.
6. In **Import shared filters**, paste the snippet below and click **Import filters**. An **Organizer tools** button appears.

   ```json
   {"category":"food","__proto__":{"isOrganizer":true}}
   ```

7. Reload the page. **Organizer tools** is still there, because the poisoned filters were saved and run again on every load.
8. Look at your **server log**. Find your token in the URLs (`?access_token=nb_...`).
9. DevTools → **Application** → **Local Storage** → `http://localhost:8080`. Your token is sitting there.
10. Click **Simulate expired session**, then **Refresh events**. Read the error message. It shows an internal server name.
11. Click **Refresh attendance**. Then wait… and wait.
12. Open **http://localhost:8081/frame.html**. Another site has loaded Neighborly in a see-through frame.
13. Back on Neighborly, click **Load partner events**. Read the console error that mentions **CORS policy**. We'll come back to it in Part 3.

**Map the trust boundary.** Fill in this table with your group. For each place untrusted data enters, where does it end up (the *sink*)?

| Source of untrusted data | Where it lands (sink) | Which part fixes it? |
|---|---|---|
| `?welcome=` in the URL | | |
| Event list from `/api/events` | | |
| The "Post an event" form | | |
| `/api/events/featured` response | | |
| `/api/events/4` response | | |
| Pasted filter JSON and `localStorage` | | |

When you're done, click **Reset demo** (under Filters). It clears saved data and reloads the page.

---

## Part 1: Content Security Policy (Lessons 1–5)

A CSP is a rule list the **browser** enforces: "only run scripts from these places." It doesn't fix bugs. It limits what an injected payload can do. And you can't turn on a strict policy until the page itself stops using inline script.

### Step 1.1: Stage a policy in Report-Only mode

Replace the contents of `headers.json` (next to `server.js`) with:

```json
{
  "Content-Security-Policy-Report-Only": "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'none'; form-action 'self'; report-uri /csp-report"
}
```

The server re-reads this file on every request, so just **reload the browser**. You don't need to restart the server.

Report-Only means the browser **reports** violations but **blocks nothing**. It's how you test a policy on a live app without breaking it. Check your server log. You'll see reports like these (the exact number can vary):

```
[csp-report] REPORT-ONLY  directive=script-src-elem  blocked=inline  at=/:11
[csp-report] REPORT-ONLY  directive=style-src-attr   blocked=inline  at=/:21
[csp-report] REPORT-ONLY  directive=script-src-attr  blocked=inline  at=/:25
```

`at=/:11` means line 11 of the page (`index.html`). Open `index.html` and find each one:

- **line 11**: an inline `<script>` that sets `window.NEIGHBORLY_CONFIG`
- **line 21**: a `style="..."` attribute on the `<h1>`
- **line 25**: `onsubmit="signIn(event)"`, the first of many inline handlers

These are **our own code**. A strict policy would break them. That's a misconfiguration we must fix before we enforce.

### Step 1.2: Move the config out of the inline script

Configuration is **data**, not code. In `public/index.html`, replace the inline script:

```html
  <!-- TODO Part 1: ... -->
  <script>
    window.NEIGHBORLY_CONFIG = { apiBase: "/api", timeoutMs: 5000, partnerFeed: "http://localhost:8081/partner/events" };
  </script>
```

with a JSON data block. The browser never runs `type="application/json"`, so the CSP has nothing to allow:

```html
  <script type="application/json" id="app-config">
    { "apiBase": "/api", "timeoutMs": 5000, "partnerFeed": "http://localhost:8081/partner/events" }
  </script>
```

In `public/app.js`, replace `const config = window.NEIGHBORLY_CONFIG;` (and its TODO comment) with:

```js
// PART 1: configuration comes from a JSON data block, not an inline script.
function readConfig() {
  const defaults = { apiBase: "/api", timeoutMs: 5000, partnerFeed: "" };
  let c;
  try {
    c = JSON.parse(document.getElementById("app-config").textContent);
  } catch {
    return defaults; // fail closed to known-good values
  }
  if (typeof c !== "object" || c === null) return defaults;
  return {
    apiBase: typeof c.apiBase === "string" ? c.apiBase : defaults.apiBase,
    timeoutMs: Number.isInteger(c.timeoutMs) && c.timeoutMs > 0 ? c.timeoutMs : defaults.timeoutMs,
    partnerFeed: typeof c.partnerFeed === "string" ? c.partnerFeed : defaults.partnerFeed,
  };
}

const config = readConfig();
```

### Step 1.3: Replace inline handlers with `addEventListener`

1. In `public/index.html`, **delete every** `onclick="..."`, `onsubmit="..."` and `onchange="..."` attribute. There are 15. Search for `onclick=`, `onsubmit=` and `onchange=` to find them all.
2. Delete `style="letter-spacing: .02em"` from the `<h1>`. That rule is already in `styles.css`.
3. In `public/app.js`, find the block that starts with `// TODO Part 1: the inline onclick...` and ends with the `Object.assign(window, {...});` call. Those globals exist only so the inline handlers can find the functions. Replace the whole block with:

```js
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
```

Reload. The three reports about `/:11`, `/:21` and `/:25` are gone. Click every button to check that it still works.

### Step 1.4: Read the reports that remain

Sign in. You'll still see reports like this one, and the payloads still **fire** (Report-Only blocks nothing):

```
[csp-report] REPORT-ONLY  directive=script-src-attr  blocked=inline  at=/render.js:31
```

This one is different. Line 31 of `render.js` is where event data is written with `innerHTML`. The inline handler came from **the data**, not from our code. That's an **attack signal**, and it's exactly what the policy is for. Don't loosen the policy to make it go away. Part 2 fixes the root cause.

### Step 1.5: Enforce it

Replace `headers.json` with the enforced policy. It adds `frame-ancestors 'none'` (no other site may frame us) and `nosniff`:

```json
{
  "Content-Security-Policy": "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; font-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'none'; form-action 'self'; frame-ancestors 'none'; report-uri /csp-report",
  "X-Content-Type-Options": "nosniff"
}
```

Reload, sign in, and redo Part 0. Check the board:

- Nothing is **FIRED** any more. The browser refuses to run the injected handlers.
- But the rows still say **LIVE**. The dangerous markup is still in the DOM. CSP is now your *only* defense, and that's one bad policy edit away from failing.
- Open **http://localhost:8081/frame.html** again. The frame now shows an error instead of Neighborly. That's `frame-ancestors` working. It only works as an HTTP header, not in a `<meta>` tag.
- Click **Load partner events**. It's now blocked by `connect-src` before CORS even comes into it. (More on this in Part 3.)

**Check yourself:** Why did we stage with Report-Only first instead of enforcing right away? What would have happened to the sign-in button?

---

## Part 2: Sanitize and render safely (Lessons 6–10)

The rule: **the context decides the defense.**

- Plain text goes in with `textContent`.
- A URL must pass a scheme allow-list.
- The one field that really needs HTML goes through DOMPurify with a tight allow-list.
- Input is validated against allow-lists, and if anything fails, nothing is used (fail closed).

### Step 2.1: A URL allow-list

In `public/render.js`, replace the `safeUrl` stub:

```js
// URL allow-list (L9, L10): only absolute http(s) links become clickable.
const SAFE_SCHEMES = new Set(["http:", "https:"]);

export function safeUrl(raw) {
  if (typeof raw !== "string") return null;
  let url;
  try {
    url = new URL(raw.trim()); // throws on relative or garbage input, so we fail closed
  } catch {
    return null;
  }
  return SAFE_SCHEMES.has(url.protocol) ? url.href : null;
}
```

We allow known-good schemes rather than block `javascript:`. A deny-list misses `JaVaScRiPt:`, `data:`, `vbscript:` and whatever comes next.

### Step 2.2: DOMPurify for the one rich-text field

Replace the `sanitizeDescription` stub:

```js
// Rich text (L8): DOMPurify with an explicit allow-list. No attributes at all.
const DESCRIPTION_POLICY = {
  ALLOWED_TAGS: ["p", "b", "i", "em", "strong", "ul", "li", "br"],
  ALLOWED_ATTR: [],
};

export function sanitizeDescription(html) {
  return window.DOMPurify.sanitize(String(html ?? ""), DESCRIPTION_POLICY);
}
```

DOMPurify is already loaded from `vendor/purify.min.js`, served from our own origin. So it fits `script-src 'self'`, and we didn't have to loosen the policy for a CDN.

### Step 2.3: Build the event card with DOM APIs

Add this small helper below `sanitizeDescription`. It can only ever create **text**:

```js
function el(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = String(text);
  return node;
}
```

Replace `renderEventCard` with:

```js
export function renderEventCard(ev, { preview = false } = {}) {
  const li = el("li", "event-card");
  li.append(
    el("h3", "event-title", ev.title),
    el("p", "event-meta", `${ev.organizer} · ${formatDate(ev.date)} · ${ev.category}`),
    el("p", "event-spots", spotsLeft(ev))
  );

  const desc = el("div", "event-description");
  desc.innerHTML = sanitizeDescription(ev.description); // the ONE acceptable innerHTML: sanitized output
  li.append(desc);

  const href = safeUrl(ev.link);
  if (href) {
    const a = el("a", "event-link", "Event website");
    a.href = href;
    a.rel = "noopener noreferrer";
    li.append(a);
  } else {
    li.append(el("span", "event-link blocked-link", "Link removed (unsafe or invalid address)"));
  }

  if (!preview) {
    const btn = el("button", "details-btn", "Details");
    btn.type = "button";
    btn.dataset.eventId = String(ev.id);
    li.append(btn);
  }
  return li;
}
```

### Step 2.4: Fix every other `innerHTML`

Use your editor's search for `innerHTML` in `render.js`. Replace each remaining function:

```js
export function renderEventList(listEl, events) {
  if (events.length === 0) {
    listEl.replaceChildren(el("li", "empty", "No events match your filters."));
    return;
  }
  listEl.replaceChildren(...events.map((ev) => renderEventCard(ev)));
}

export function renderDetail(detailEl, ev) {
  const dl = el("dl", "detail-list");
  for (const [term, value] of [
    ["Where", ev.location],
    ["When", formatDate(ev.date)],
    ["Organizer", ev.organizer],
    ["Capacity", ev.capacity],
  ]) {
    dl.append(el("dt", "", term), el("dd", "", value));
  }
  detailEl.replaceChildren(el("h3", "", ev.title), dl);
}

export function renderFeatured(featuredEl, ev) {
  const card = el("div", "featured-card");
  card.append(el("h3", "", `Featured: ${ev.title}`), el("p", "", formatDate(ev.date)));
  featuredEl.replaceChildren(card);
}

export function renderBanner(bannerEl, name) {
  bannerEl.textContent = `Welcome, ${name}! Thanks for following a friend's invite.`;
  bannerEl.hidden = false;
}

export function renderGreeting(greetingEl, user) {
  greetingEl.textContent = `Signed in as ${user.name} (${user.role})`;
}

export function showMessage(target, text, kind = "info") {
  target.textContent = text;
  target.dataset.kind = kind;
}

export function showErrors(target, errors) {
  if (!errors.length) {
    target.replaceChildren();
    return;
  }
  const ul = el("ul");
  for (const e of errors) ul.append(el("li", "", e));
  target.replaceChildren(ul);
}
```

When you're done, the only `innerHTML` left in `render.js` should be the sanitized description.

In `public/app.js`, near the bottom, limit the banner name's length as it comes in:

```js
if (welcome) renderBanner($("welcome-banner"), welcome.trim().slice(0, 60)); // Part 2: limit length, render as text
```

### Step 2.5: Validate the form at the boundary

In `public/form.js`, add the category allow-list under the existing `import` line. It reuses the list in
`safe-json.js`, so the form and the data contract can never disagree:

```js
import { CATEGORIES as CATEGORY_LIST } from "./safe-json.js";

const CATEGORIES = new Set(CATEGORY_LIST); // one source of truth for the allow-list
```

Then replace `validateDraft`:

```js
export function validateDraft(raw) {
  const errors = [];

  const title = raw.title.trim();
  if (title.length < 3 || title.length > 80) errors.push("Title must be 3–80 characters.");

  const category = raw.category;
  if (!CATEGORIES.has(category)) errors.push("Choose a category from the list.");

  const when = new Date(raw.date); // datetime-local gives "YYYY-MM-DDTHH:MM"
  if (!raw.date || Number.isNaN(when.getTime())) errors.push("Enter a valid date and time.");

  const capacity = Number(raw.capacity);
  if (!Number.isInteger(capacity) || capacity < 1 || capacity > 500) errors.push("Capacity must be a whole number from 1 to 500.");

  const link = safeUrl(raw.link);
  if (!link) errors.push("Link must be a full http:// or https:// address.");

  const description = raw.description;
  if (description.length > 2000) errors.push("Description must be 2000 characters or fewer.");
  // We do NOT try to "clean" the description here. It stays as typed and is
  // sanitized at output by DOMPurify (render.js), where we know the context.

  if (errors.length) return { ok: false, errors };
  return {
    ok: true,
    value: { title, category, date: when.toISOString(), capacity, link, description },
  };
}
```

Notice what this function does *not* do: it doesn't strip tags from the title with a regex. Validation checks the shape, and output encoding (`textContent`) makes the title safe wherever it's shown.

**Check it:** reload and sign in. Board rows 1–5 should read **blocked**. Try your own payloads in **Post an event**, for example a title of `<img src=x onerror="__breakit('preview')">` and a link of `javascript:alert(1)`. The title shows as plain text, and the link is refused.

**Check yourself:** The hostile event's title passes every *type* check (it's a string). Why is `textContent` still the right fix, and not more validation?

---

## Part 3: Secure fetch (Lessons 11–15)

Right now `app.js` calls `fetch()` in eight places. Each one makes its own mistakes: the token goes in the URL, nothing checks `res.ok`, there's no timeout, and server error text is shown to users. You'll build **one** wrapper and route every call through it.

### Step 3.1: Build `apiRequest`

In `public/api.js`, replace the `apiRequest` stub (keep everything above it) with a small helper and the wrapper:

```js
function failedToAnswer(err) {
  return new ApiError(
    err && err.name === "AbortError"
      ? "The server took too long to answer. Please try again."
      : "Network problem. Please check your connection and try again.",
    0
  );
}

export async function apiRequest(path, { method = "GET", body, validate, timeoutMs } = {}) {
  // L15: only same-origin paths under apiBase. This matches CSP connect-src 'self'.
  if (typeof path !== "string" || !path.startsWith("/") || path.startsWith("//") || path.includes("..")) {
    throw new ApiError("That data source isn't on our allow-list.", 0);
  }

  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers["Authorization"] = `Bearer ${token}`; // header, never ?access_token=

  // L11/L15: a request that never answers must not hang the UI forever.
  // The timer covers the WHOLE exchange, including reading the body.
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs ?? settings.timeoutMs);

  try {
    let res;
    try {
      res = await fetch(settings.apiBase + path, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: controller.signal,
        credentials: "same-origin",
        cache: "no-store",
      });
    } catch (err) {
      throw failedToAnswer(err);
    }

    // L13: an expired or missing token means sign out cleanly.
    if (res.status === 401) {
      clearToken();
      settings.onUnauthorized();
      throw new ApiError("Your session has ended. Please sign in again.", 401);
    }

    // L11/L14: a non-2xx status is a failure. Log the status, but never show
    // the server's error body to the user.
    if (!res.ok) {
      console.warn(`[api] ${method} ${path} failed with status ${res.status}`);
      throw new ApiError("Something went wrong. Please try again.", res.status);
    }
    if (res.status === 204) return null;

    // L14: check the Content-Type before trusting the body as JSON.
    const type = res.headers.get("Content-Type") || "";
    if (!/^application\/json\b/i.test(type)) {
      console.warn(`[api] ${path}: expected JSON, got "${type}". Response discarded.`);
      throw new ApiError("We got an unexpected response, so we didn't show it.", res.status);
    }

    let text;
    try {
      text = await res.text();
    } catch (err) {
      throw failedToAnswer(err);
    }

    // PART 4 (L16–17): guarded parse with a filtering reviver.
    const parsed = parseUntrusted(text);
    if (!parsed.ok) throw new ApiError("We got a damaged response, so we didn't show it.", res.status);

    // L14/L19: the caller says what shape it expects. Nothing unvalidated escapes.
    if (validate) {
      const result = validate(parsed.value);
      if (!result.ok) {
        console.warn(`[api] ${path}: response failed validation:`, result.errors);
        throw new ApiError("We got data we couldn't verify, so we didn't show it.", res.status);
      }
      return result.value;
    }
    return parsed.value;
  } finally {
    clearTimeout(timer);
  }
}
```

The token lives in the module variable `let token = null` at the top of `api.js`. Nothing else in the app can read it, and it's never written to storage.

The password gets the same care. It travels only in a POST body (never a URL), the field is cleared after sign-in, and the server keeps only a salted hash (see `hashPassword` in `server.js`). In production this traffic must also run over HTTPS.

### Step 3.2: Route `app.js` through the wrapper

**a. Imports.** At the top of `public/app.js`, add these imports. The `validate` functions are pass-through stubs until Part 4, but we wire them in now so that *every* call declares the shape it expects.

```js
import * as api from "./api.js";
import {
  validate, validateList,
  EVENT_SCHEMA, EVENT_DETAIL_SCHEMA, SESSION_SCHEMA, ATTENDANCE_SCHEMA,
} from "./safe-json.js";
```

**b. Stop storing the session.**

- Delete the line `const SESSION_KEY = "neighborly.session";`.
- Delete `token: null,` from `state`.
- Delete `state.token = null;` from `showSignedOut`.
- Change the comment above `STORAGE_KEY` to `// TODO Part 4: saved filters are untrusted text. Parse, merge and validate them safely.`

**c. Configure the wrapper once.** Directly after the `showSignedOut` function, add:

```js
// PART 3: one wrapper, configured once.
api.configure({
  apiBase: config.apiBase,
  timeoutMs: config.timeoutMs,
  onUnauthorized: () => showSignedOut("Your session has ended. Please sign in again."),
});

function report(err) {
  // Only ApiError messages are written for users. Anything else stays generic.
  showMessage(status, err instanceof api.ApiError ? err.message : "Something went wrong.", "error");
  if (!(err instanceof api.ApiError)) console.error(err);
}
```

**d. Replace the network functions (copy-paste).** Replace `signIn`, `createAccount`, `loadEvents`, `showDetails`, `loadFeatured`, `refreshAttendance`, `loadPartnerEvents`, `expireSession` and `signOut` with the code below. The new `readCredentials` helper goes above `signIn`:

```js
// L7: allow-list the username format and bound the password length before anything is sent.
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
    api.setToken(session.token); // memory only: no localStorage, no console.log
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
```

**e. Replace `postEvent` (copy-paste):**

```js
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
```

**f. Fix the `resetDemo` line and the start-up code.**

- In `resetDemo`, change `localStorage.removeItem(SESSION_KEY);` to `localStorage.removeItem("neighborly.session");`. This clears out sessions saved before the fix.
- Replace everything from `const saved = localStorage.getItem(SESSION_KEY);` to the end of the file with:

```js
showMessage(status, "Sign in to see events.");
```

A reload now signs you out. That's the trade-off for keeping the token out of reach of injected scripts. (In production, an `HttpOnly`, `Secure`, `SameSite` cookie set by the server is the usual way to keep a login across reloads.)

**g. Make the details click handler stricter.** Replace the `$("event-list").addEventListener(...)` block with:

```js
$("event-list").addEventListener("click", (e) => {
  const btn = e.target instanceof Element ? e.target.closest("button[data-event-id]") : null;
  if (!btn) return;
  const id = Number(btn.dataset.eventId);
  if (Number.isInteger(id) && id > 0) showDetails(id);
});
```

**Check it.** Click **Reset demo**, then sign in.

- **Load featured** says "We got an unexpected response…". The console shows the `Content-Type` warning.
- The server log has no `access_token=` in any URL, and Local Storage holds no token.
- **Simulate expired session**, then **Refresh events**: you're signed out with a plain message. No internal server name is shown.
- **Refresh attendance** gives up after 5 seconds with a friendly message.
- Sign out, then sign in with a wrong password. The message is the same one you'd get for a username that doesn't exist, so sign-in can't be used to find out who has an account.
- Board rows 6 and 7 read **blocked**.

### Discussion: CORS (Lesson 12)

Think back to Part 0, step 13. **Load partner events** failed with: *"…has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present…"*. Then, after Part 1, `connect-src` blocked the same request even earlier.

- Whose decision is CORS: the browser's, our page's, or the partner server's?
- A teammate suggests "just proxy their API through our server." What protection does that throw away?
- If we really needed partner data, what **two** changes are required? (Hint: one on their server, one in our `headers.json`.)

---

## Part 4: Secure JSON (Lessons 16–19)

`JSON.parse` only proves the text was well-formed. It doesn't prove the data is safe to merge, or that it has the shape you expect. You'll build the pipeline that every piece of untrusted JSON now goes through:

```
parse (try/catch) → reviver (drop dangerous keys, revive dates) → safe merge → schema → use
```

### Step 4.1: The reviver and the guarded parse

In `public/safe-json.js`, replace the `safeReviver` and `parseUntrusted` stubs:

```js
const DANGEROUS_KEYS = new Set(["__proto__", "constructor", "prototype"]);
const DATE_KEYS = new Set(["date"]); // only fields our contracts say are dates
const ISO_DATE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z$/;

// L17: the reviver runs once per key, bottom-up. Returning undefined deletes the key.
export function safeReviver(key, value) {
  if (DANGEROUS_KEYS.has(key)) return undefined;
  if (DATE_KEYS.has(key) && typeof value === "string" && ISO_DATE.test(value)) {
    const d = new Date(value);
    return Number.isNaN(d.getTime()) ? undefined : d;
  }
  return value;
}

// L16: JSON.parse (never eval) wrapped in try/catch. Bad input returns { ok: false }.
export function parseUntrusted(text) {
  if (typeof text !== "string") return { ok: false, error: "not text" };
  try {
    return { ok: true, value: JSON.parse(text, safeReviver) };
  } catch {
    return { ok: false, error: "malformed JSON" };
  }
}
```

`apiRequest` already calls `parseUntrusted`, so every API response now passes through the reviver too.

Why only the `date` key? If the reviver turned *every* ISO-looking string into a `Date`, an event titled
`2026-11-20T00:30:00Z` would stop being text and fail the schema. Decide types by field, not by what a value looks like.

### Step 4.2: A replacer so secrets never get serialized

Replace the `withoutSecrets` stub:

```js
const SECRET_KEYS = new Set(["token", "password", "authorization"]);

export function withoutSecrets(key, value) {
  return SECRET_KEYS.has(String(key).toLowerCase()) ? undefined : value;
}
```

### Step 4.3: A merge that can't reach the prototype

Replace the `safeMerge` stub:

```js
export function safeMerge(target, source) {
  const out = Object.create(null);
  if (isPlainObject(target)) {
    for (const key of Object.keys(target)) out[key] = target[key];
  }
  if (!isPlainObject(source)) return out;
  for (const key of Object.keys(source)) {
    if (DANGEROUS_KEYS.has(key)) continue;
    const incoming = source[key];
    // Nested objects are always rebuilt through safeMerge, so their keys get filtered too.
    out[key] = isPlainObject(incoming) ? safeMerge(out[key], incoming) : incoming;
  }
  return out;
}
```

Two defenses are stacked here. The denylist refuses the dangerous key names. And the `Object.create(null)` result has no prototype to reach, even if a key slipped past.

### Step 4.4: Schema validation (copy-paste the `validate` function, then type the two contracts)

Replace the `validate` stub. `checkRule` (already provided above it) checks one value. `validate` checks the whole object:

```js
export function validate(schema, data) {
  if (!isPlainObject(data)) return { ok: false, errors: ["expected an object"] };
  const errors = [];
  for (const key of Object.keys(data)) {
    if (!Object.hasOwn(schema, key)) errors.push(`unexpected field "${key}"`);
  }
  const value = Object.create(null);
  for (const [key, rule] of Object.entries(schema)) {
    if (!Object.hasOwn(data, key)) {
      if (rule.required) errors.push(`missing "${key}"`);
      continue;
    }
    const problem = checkRule(rule, data[key]);
    if (problem) errors.push(`"${key}" ${problem}`);
    else value[key] = rule.type === "object" ? validate(rule.schema, data[key]).value : data[key];
  }
  return errors.length ? { ok: false, errors } : { ok: true, value };
}
```

The first loop is the one that catches smuggled fields. Extra keys are **rejected**, not silently ignored.

Now finish the two contracts at the bottom of the file:

```js
// The detail view also returns "location".
export const EVENT_DETAIL_SCHEMA = {
  ...EVENT_SCHEMA,
  location: { type: "string", required: true, minLength: 1, maxLength: 120 },
};
```

```js
export const FILTERS_SCHEMA = {
  category: { type: "enum", required: true, values: ["all", ...CATEGORIES] },
  sortBy: { type: "enum", required: true, values: ["soonest", "latest"] },
};
```

### Step 4.5: Use the pipeline in `app.js` (copy-paste)

**a.** Change the `safe-json.js` import at the top to:

```js
import {
  parseUntrusted, safeMerge, isPlainObject, validate, validateList, withoutSecrets,
  EVENT_SCHEMA, EVENT_DETAIL_SCHEMA, SESSION_SCHEMA, FILTERS_SCHEMA, ATTENDANCE_SCHEMA,
} from "./safe-json.js";
```

**b.** Delete the whole `deepMerge` function. Replace `loadSavedFilters` with these, and add `saveFilters` below it:

```js
const DEFAULT_FILTERS = Object.freeze({ category: "all", sortBy: "soonest" });

function loadSavedFilters() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return safeMerge(DEFAULT_FILTERS, {});
  const parsed = parseUntrusted(raw);
  const saved = parsed.ok && isPlainObject(parsed.value) ? parsed.value.filters : undefined;
  const checked = validate(FILTERS_SCHEMA, safeMerge(DEFAULT_FILTERS, saved));
  if (!checked.ok) {
    console.warn("[prefs] saved filters were invalid, so defaults are used:", checked.errors);
    return safeMerge(DEFAULT_FILTERS, {});
  }
  return checked.value;
}

function saveFilters() {
  // The replacer guarantees no secret is ever written, even if one sneaks into state.
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ filters: state.filters }, withoutSecrets));
}
```

**c.** Replace `importFilters` and `changeFilter`:

```js
function importFilters() {
  const parsed = parseUntrusted($("filters-json").value);
  if (!parsed.ok || !isPlainObject(parsed.value)) {
    showMessage(status, "Those filters aren't valid JSON.", "error");
    return;
  }
  const checked = validate(FILTERS_SCHEMA, safeMerge(state.filters, parsed.value));
  if (!checked.ok) {
    showMessage(status, `Filters rejected: ${checked.errors.join("; ")}`, "error");
    return;
  }
  state.filters = checked.value;
  saveFilters();
  syncFilterControls();
  renderList();
  showMessage(status, "Filters imported.");
}

function changeFilter() {
  const checked = validate(
    FILTERS_SCHEMA,
    safeMerge(state.filters, { category: $("filter-category").value, sortBy: $("filter-sort").value })
  );
  if (!checked.ok) return;
  state.filters = checked.value;
  saveFilters();
  renderList();
}
```

**d.** Base authorization on a validated field the server assigned, not on whatever a response happens to contain.

In `showSignedIn`, replace the `organizer-tools` line with:

```js
  // PART 4: authorization uses a validated OWN field, never an inherited one.
  $("organizer-tools").hidden = state.user.role !== "organizer";
```

In `showDetails`, replace the `edit-event` line with:

```js
    // Only the server-assigned role can unlock editing. Response fields can't.
    $("edit-event").hidden = state.user?.role !== "organizer";
```

**Check it.** Click **Reset demo** first, because earlier experiments may have saved poisoned filters. Then:

- Sign in and click **Details** on the pottery event. You'll see "We got data we couldn't verify…", and the console lists `unexpected field "canEdit"` and the wrong-typed `capacity`.
- Import `{"category":"food","__proto__":{"isOrganizer":true}}`. Only `category` is applied, and **Organizer tools** never appears.
- Try `{"constructor":{"prototype":{"isOrganizer":true}}}` and `{"category":"food","extra":1}` as well.
- Run `({}).isOrganizer` in the console. It should be `undefined`.
- Board rows 8 and 9 read **blocked**.

---

## Final check: re-attack everything

Click **Reset demo**, then repeat Part 0 steps 1, 2, 3, 4, 5, 6 and 12. (Do all 13 steps on your own later if you like.)

- [ ] Every Break-it board row reads **blocked**.
- [ ] Nothing in the console says `[break-it] … EXECUTED`.
- [ ] No `access_token=` in the server log, and no token in Local Storage.
- [ ] `http://localhost:8081/frame.html` can't show Neighborly.
- [ ] The CSP line on the board shows **ENFORCED**.

**About the CSP reports you still see.** You'll still see a few reports like this one:

```
[csp-report] ENFORCED  directive=script-src-attr  blocked=inline  at=/vendor/purify.min.js:2
```

That's the hostile event description. DOMPurify parses it in a detached document so it can remove the `onerror`. Chrome reports the handler it saw during that parse, even though nothing could have run. The page is fine. This is the policy **telling you someone is sending you attack markup**. That's why teams keep CSP reporting on after they go live. Reports pointing at **your own** files and lines are bugs to fix. Reports like this one are signals to watch.

## Key takeaways

| Layer | What it guarantees | What it can't do alone |
|---|---|---|
| **CSP** (Part 1) | Injected inline script won't run, data goes only to allowed places, and no other site can frame you | Fix the bug. The markup is still in your page. |
| **Sanitize and render** (Part 2) | Untrusted values show as text, links are only http(s), and rich text is allow-listed | Protect anything except your own rendering. The server must validate too. |
| **Secure fetch** (Part 3) | One place handles auth, timeouts, status, content type and safe error messages | Decide whether the JSON inside is the shape you expect |
| **Secure JSON** (Part 4) | No prototype pollution, no smuggled fields, typed values, and no secrets serialized | Make a type-correct string safe to put in HTML. That's Part 2's job. |

No single layer is enough. Each one covers a gap the others leave open.

## Apply it

Work through these after class. They take today's ideas into new situations.

1. A teammate adds a "website" field to user profiles and renders it with `a.href = profile.website`. Which two of today's functions would you reuse, and what would you test it with?
2. Your CSP report log suddenly fills with `connect-src` reports for `https://stats.example-analytics.com` from your own `app.js`. Is that an attack signal or a misconfiguration? What do you check first?
3. A new endpoint returns `{"items": [...], "total": 42}`. Write the schema for the outer object. What happens with your `validate` if the API later adds a `"nextPage"` field, and is that what you want?
4. Product asks to "keep users signed in across reloads." Which storage option from Lesson 13 would you propose, and what has to change on the **server**?
