#!/usr/bin/env node
// Neighborly dev server: zero dependencies, Node 18+.
//
//   node server.js
//
// Starts TWO local servers:
//   http://localhost:8080  Neighborly (the app you are hardening)
//   http://localhost:8081  "Elsewhere", a different origin used for the CORS
//                          and clickjacking demos
//
// What it does for the app on :8080
//   * Serves the files in ./public.
//   * Adds every header listed in ./headers.json to each response. The file
//     is re-read on every request, so you can edit your Content-Security-Policy
//     and just reload the browser. You don't need to restart the server.
//   * POST /csp-report: prints CSP violation reports to THIS terminal.
//   * A mock API under /api (see the route table below).
//
// You do not need to change this file during the code-along.
// Everything here is local and benign. Nothing contacts a real external site.
"use strict";

const http = require("http");
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

const APP_PORT = Number(process.env.PORT) || 8080;
const ELSEWHERE_PORT = Number(process.env.ELSEWHERE_PORT) || 8081;
const PUBLIC_DIR = path.join(__dirname, "public");
const HEADERS_FILE = path.join(__dirname, "headers.json");

const MIME = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml",
  ".png": "image/png",
  ".ico": "image/x-icon",
};

// ---------------------------------------------------------------------------
// In-memory data (events, accounts, sessions). It resets every time the server restarts.
// ---------------------------------------------------------------------------
const CATEGORIES = ["community", "learning", "volunteering", "sports", "arts", "food"];

function seedEvents() {
  return [
    {
      id: 1, title: "Saturday Park Cleanup", organizer: "Friends of Riverside Park",
      category: "volunteering", date: "2026-11-14T14:00:00.000Z", capacity: 40, spotsTaken: 12,
      link: "https://example.org/riverside-cleanup",
      description: "<p>Bring <b>gloves</b> and a water bottle. Bags and grabbers provided.</p>",
      location: "Riverside Park, north gate",
    },
    {
      id: 2, title: "Library Coding Club", organizer: "Maple Street Library",
      category: "learning", date: "2026-11-18T23:00:00.000Z", capacity: 20, spotsTaken: 17,
      link: "https://example.org/coding-club",
      description: "<p>Beginner-friendly. This week: <em>building a to-do list</em>.</p><ul><li>Laptops available</li><li>Snacks</li></ul>",
      location: "Maple Street Library, room 2",
    },
    {
      // HOSTILE: someone posted this through an older version of the site
      // that did no checking at all. Every field still has the right TYPE.
      id: 3,
      title: "Free Pizza Night <img src=x onerror=\"__breakit('event-title')\">",
      organizer: "Totally Legit Events",
      category: "food", date: "2026-11-20T00:30:00.000Z", capacity: 100, spotsTaken: 3,
      link: "javascript:__breakit('event-link')",
      description: "<p>Everyone <em>welcome</em>!</p><img src=x onerror=\"__breakit('event-description')\">",
      location: "Somewhere downtown",
    },
    {
      id: 4, title: "Wheel-Thrown Pottery Taster", organizer: "Cedar Creek Studio",
      category: "arts", date: "2026-11-22T17:00:00.000Z", capacity: 8, spotsTaken: 5,
      link: "https://example.org/pottery-taster",
      description: "<p>Make <strong>two pieces</strong> to take home after firing.</p>",
      location: "Cedar Creek Studio",
    },
    {
      id: 5, title: "Winter Farmers Market", organizer: "Neighborly Growers Co-op",
      category: "food", date: "2026-11-28T15:00:00.000Z", capacity: 300, spotsTaken: 0,
      link: "https://example.org/winter-market",
      description: "<p>Local produce, bread, and <i>hot cider</i>.</p>",
      location: "Old Mill parking lot",
    },
    {
      id: 6, title: "Couch-to-5K Kickoff", organizer: "Eastside Runners",
      category: "sports", date: "2026-12-01T12:00:00.000Z", capacity: 50, spotsTaken: 21,
      link: "https://example.org/c25k",
      description: "<p>All paces welcome. We walk as much as we run.</p>",
      location: "Eastside track",
    },
  ];
}

let events = seedEvents();
let nextId = 7;
const sessions = new Map(); // token -> { name, role }

// Accounts: passwords are never stored or logged, only a salted scrypt hash.
const users = new Map(); // lower-cased username -> { username, salt, hash, role }
const USERNAME_RE = /^[A-Za-z0-9_-]{3,20}$/;
const DUMMY_SALT = crypto.randomBytes(16);

function hashPassword(password, salt) {
  return new Promise((resolve, reject) =>
    crypto.scrypt(password, salt, 64, (err, key) => (err ? reject(err) : resolve(key))));
}

function listItem(e) {
  // The list view omits "location". Only the detail endpoint returns it.
  const { location, ...rest } = e;
  return rest;
}

function detailItem(e) {
  if (e.id === 4) {
    // BUG/HOSTILE: a backend change for this record returns a wrong-typed
    // capacity and a smuggled "canEdit" flag the client never asked for.
    return { ...e, capacity: "lots", canEdit: true };
  }
  return { ...e };
}

// ---------------------------------------------------------------------------
// helpers
// ---------------------------------------------------------------------------
function sendJson(res, status, body, extraHeaders = {}) {
  res.writeHead(status, { "Content-Type": "application/json; charset=utf-8", ...extraHeaders });
  res.end(JSON.stringify(body));
}

function readBody(req, limit = 64 * 1024) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    req.on("data", (c) => {
      size += c.length;
      if (size > limit) { reject(new Error("body too large")); req.destroy(); return; }
      chunks.push(c);
    });
    req.on("end", () => resolve(Buffer.concat(chunks).toString("utf8")));
    req.on("error", reject);
  });
}

function loadHeaders() {
  try {
    const parsed = JSON.parse(fs.readFileSync(HEADERS_FILE, "utf8"));
    const out = {};
    for (const [k, v] of Object.entries(parsed)) {
      if (typeof v === "string" && k !== "__proto__") out[k] = v;
    }
    return out;
  } catch (err) {
    if (err.code !== "ENOENT") console.error("[serve] headers.json is not valid JSON, so no extra headers were sent:", err.message);
    return {};
  }
}

// Accepts the token from "Authorization: Bearer ..." OR (the legacy way) from
// "?access_token=..." in the URL. The URL form exists ONLY so the insecure
// starter runs, and it leaks the token into this server's request log below.
// A real server would retire it once every client sends the header.
function sessionFor(req, url) {
  const auth = req.headers["authorization"] || "";
  const m = /^Bearer (\S+)$/.exec(auth);
  const token = m ? m[1] : url.searchParams.get("access_token");
  return token && sessions.has(token) ? { token, ...sessions.get(token) } : null;
}

function serverValidateEvent(body) {
  // Server-side validation: the client-side checks are a layer, never the only one.
  const errors = [];
  if (typeof body !== "object" || body === null || Array.isArray(body)) return ["body must be an object"];
  if (typeof body.title !== "string" || body.title.length < 3 || body.title.length > 80) errors.push("title");
  if (!CATEGORIES.includes(body.category)) errors.push("category");
  if (typeof body.date !== "string" || Number.isNaN(Date.parse(body.date))) errors.push("date");
  if (!Number.isInteger(body.capacity) || body.capacity < 1 || body.capacity > 500) errors.push("capacity");
  if (typeof body.link !== "string" || body.link.length > 300) errors.push("link");
  if (typeof body.description !== "string" || body.description.length > 2000) errors.push("description");
  return errors;
}

// ---------------------------------------------------------------------------
// Mock API (/api/*)
//
//   POST /api/accounts             {username, password} -> 201 {username} | 400 | 409 taken
//   POST /api/session              {username, password} -> {token, user:{name, role}} | 401
//   POST /api/session/expire       (auth)   -> revokes your token (simulates expiry)
//   GET  /api/events               (auth)   -> event list (JSON)
//   POST /api/events               (auth)   -> create an event
//   GET  /api/events/featured      (auth)   -> 200 text/html  (a misconfigured proxy page!)
//   GET  /api/events/:id           (auth)   -> one event, with location
//   GET  /api/attendance           (auth)   -> hangs for 20 s before answering
// ---------------------------------------------------------------------------
async function handleApi(req, res, url) {
  const p = url.pathname;

  if (p === "/api/accounts" && req.method === "POST") {
    let body;
    try { body = JSON.parse(await readBody(req)); } catch { return sendJson(res, 400, { error: "invalid JSON" }); }
    const username = body && typeof body.username === "string" ? body.username : "";
    const password = body && typeof body.password === "string" ? body.password : "";
    if (!USERNAME_RE.test(username)) return sendJson(res, 400, { error: "username must be 3-20 letters, digits, _ or -" });
    if (password.length < 8 || password.length > 72) return sendJson(res, 400, { error: "password must be 8-72 characters" });
    const key = username.toLowerCase();
    if (users.has(key)) return sendJson(res, 409, { error: "username is taken" });
    const salt = crypto.randomBytes(16);
    users.set(key, { username, salt, hash: await hashPassword(password, salt), role: "member" });
    console.log(`[app] account created: ${username}`);
    return sendJson(res, 201, { username });
  }

  if (p === "/api/session" && req.method === "POST") {
    let body;
    try { body = JSON.parse(await readBody(req)); } catch { return sendJson(res, 400, { error: "invalid JSON" }); }
    const username = body && typeof body.username === "string" ? body.username : "";
    const password = body && typeof body.password === "string" ? body.password.slice(0, 72) : "";
    const account = users.get(username.toLowerCase());
    // Hash even when the user doesn't exist, so response timing doesn't reveal which usernames are real.
    const hash = await hashPassword(password, account ? account.salt : DUMMY_SALT);
    if (!account || !crypto.timingSafeEqual(hash, account.hash)) {
      return sendJson(res, 401, { error: "invalid username or password" });
    }
    const token = "nb_" + crypto.randomBytes(16).toString("hex");
    const user = { name: account.username, role: account.role };
    sessions.set(token, user);
    return sendJson(res, 201, { token, user });
  }

  const session = sessionFor(req, url);
  if (!session) return sendJson(res, 401, { error: "missing or expired token", hint: "internal: auth-gw-02 rejected the request" });

  if (p === "/api/session/expire" && req.method === "POST") {
    sessions.delete(session.token);
    res.writeHead(204).end();
    return;
  }

  if (p === "/api/events" && req.method === "GET") return sendJson(res, 200, events.map(listItem));

  if (p === "/api/events" && req.method === "POST") {
    let body;
    try { body = JSON.parse(await readBody(req)); } catch { return sendJson(res, 400, { error: "invalid JSON" }); }
    const errors = serverValidateEvent(body);
    if (errors.length) return sendJson(res, 422, { error: "validation failed", fields: errors });
    const created = {
      id: nextId++, title: body.title, organizer: session.name, category: body.category,
      date: new Date(body.date).toISOString(), capacity: body.capacity, spotsTaken: 0,
      link: body.link, description: body.description, location: "TBA",
    };
    events.push(created);
    return sendJson(res, 201, listItem(created));
  }

  if (p === "/api/events/featured" && req.method === "GET") {
    // A misconfigured caching proxy answers with a 200 HTML page instead of JSON.
    res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    return res.end(
      "<div class=\"featured-card\"><h3>Featured: Night Market</h3>" +
      "<img src=x onerror=\"__breakit('featured-html')\">" +
      "<p>Proxy cache node edge-7 &mdash; upstream timeout</p></div>"
    );
  }

  const idMatch = /^\/api\/events\/(\d+)$/.exec(p);
  if (idMatch && req.method === "GET") {
    const found = events.find((e) => e.id === Number(idMatch[1]));
    if (!found) return sendJson(res, 404, { error: "no such event" });
    return sendJson(res, 200, detailItem(found));
  }

  if (p === "/api/attendance" && req.method === "GET") {
    const timer = setTimeout(() => sendJson(res, 200, { checkedIn: 37 }), 20000);
    req.on("close", () => clearTimeout(timer));
    return;
  }

  return sendJson(res, 404, { error: "no such endpoint" });
}

// ---------------------------------------------------------------------------
// App server (:8080)
// ---------------------------------------------------------------------------
const app = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://localhost:${APP_PORT}`);
  // Request log. Notice what shows up here if a token is put in the URL.
  console.log(`[app] ${req.method} ${req.url}`);

  try {
    if (url.pathname === "/csp-report" && req.method === "POST") {
      const raw = await readBody(req);
      let r = {};
      try { r = JSON.parse(raw)["csp-report"] || {}; } catch { /* ignore malformed reports */ }
      const mode = r.disposition === "report" ? "REPORT-ONLY" : "ENFORCED";
      console.log(
        `[csp-report] ${mode}  directive=${r["effective-directive"] || r["violated-directive"]}` +
        `  blocked=${r["blocked-uri"] || "?"}  at=${(r["source-file"] || "").replace(/^https?:\/\/[^/]+/, "")}:${r["line-number"] || "?"}` +
        (r["script-sample"] ? `  sample="${r["script-sample"]}"` : "")
      );
      res.writeHead(204).end();
      return;
    }

    if (url.pathname.startsWith("/api/")) return await handleApi(req, res, url);

    if (req.method !== "GET" && req.method !== "HEAD") { res.writeHead(405).end(); return; }

    let rel = decodeURIComponent(url.pathname);
    if (rel.endsWith("/")) rel += "index.html";
    const fp = path.normalize(path.join(PUBLIC_DIR, rel));
    if (!fp.startsWith(PUBLIC_DIR + path.sep)) { res.writeHead(403).end("forbidden"); return; }

    fs.readFile(fp, (err, data) => {
      if (err) { res.writeHead(404, { "Content-Type": "text/plain" }).end("not found"); return; }
      const base = {
        "Content-Type": MIME[path.extname(fp)] || "application/octet-stream",
        "Cache-Control": "no-store",
      };
      try {
        res.writeHead(200, { ...base, ...loadHeaders() });
      } catch (headerErr) {
        // A typo in headers.json (for example a line break inside a value) must not crash the server.
        console.error("[serve] headers.json has an invalid header, so it was skipped:", headerErr.message);
        res.writeHead(200, base);
      }
      res.end(req.method === "HEAD" ? undefined : data);
    });
  } catch (err) {
    console.error("[app] error:", err.message);
    if (!res.headersSent) sendJson(res, 500, { error: "internal error" });
  }
});

// ---------------------------------------------------------------------------
// "Elsewhere" server (:8081), a DIFFERENT origin
//   GET /partner/events -> JSON, but with NO Access-Control-Allow-Origin header
//   GET /frame.html     -> a page that tries to frame Neighborly (clickjacking)
// ---------------------------------------------------------------------------
const elsewhere = http.createServer((req, res) => {
  const url = new URL(req.url, `http://localhost:${ELSEWHERE_PORT}`);
  console.log(`[elsewhere] ${req.method} ${req.url}  Origin: ${req.headers.origin || "(none)"}`);

  if (url.pathname === "/partner/events") {
    // No CORS headers: the browser will refuse to hand this to a page on :8080.
    return sendJson(res, 200, [{ id: 900, title: "Partner Gala" }]);
  }
  if (url.pathname === "/" || url.pathname === "/frame.html") {
    res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    return res.end(`<!doctype html>
<html lang="en"><head><meta charset="utf-8"><title>WIN A FREE TABLET</title>
<style>
  body { font: 16px system-ui, sans-serif; margin: 2rem; background: #fff7e6; }
  .bait { position: relative; width: 920px; height: 620px; border: 3px dashed #d97706; }
  .bait iframe { width: 100%; height: 100%; border: 0; opacity: 0.35; }
  .bait .prize { position: absolute; top: 18px; right: 30px; background: #dc2626; color: #fff;
                 padding: .6rem 1rem; font-weight: 700; pointer-events: none; }
</style></head>
<body>
  <h1>Click the red button to claim your prize!</h1>
  <p>(Clickjacking demo. This page lives on <code>localhost:${ELSEWHERE_PORT}</code> and tries to load
     Neighborly from <code>localhost:${APP_PORT}</code> in a see-through iframe.)</p>
  <div class="bait">
    <iframe src="http://localhost:${APP_PORT}/" title="framed Neighborly"></iframe>
    <div class="prize">CLAIM PRIZE</div>
  </div>
</body></html>`);
  }
  res.writeHead(404).end("not found");
});

app.listen(APP_PORT, () => {
  console.log(`[serve] Neighborly   http://localhost:${APP_PORT}`);
  console.log(`[serve] headers from ${path.relative(process.cwd(), HEADERS_FILE) || "headers.json"} (re-read on every request)`);
});
elsewhere.listen(ELSEWHERE_PORT, () => {
  console.log(`[serve] Elsewhere    http://localhost:${ELSEWHERE_PORT}/frame.html  (other-origin demos)`);
});
