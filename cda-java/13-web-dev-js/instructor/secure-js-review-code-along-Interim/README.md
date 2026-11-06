# Neighborly: Secure JavaScript Review Code-Along

A small community event board that is insecure in all four layers of *Web Development with JavaScript — Secure-by-Design*. You'll harden it step by step. Follow **[CODE-ALONG.md](CODE-ALONG.md)**.

## Run it

You need **Node.js 18+** and Chrome or Edge. There's nothing to install.

```
node server.js
```

Then open **http://localhost:8080** with DevTools open.

The server prints every request and every CSP report in its terminal. Keep that terminal visible, because you'll read it during the code-along. It also starts a second server on **http://localhost:8081**. That one stands in for "another website" in the CORS and clickjacking demos.

## Files

| File | What it is | Do you edit it? |
|---|---|---|
| `server.js` | Zero-dependency dev server. It serves `public/`, adds the headers from `headers.json`, prints CSP reports, and provides the mock `/api` | No |
| `headers.json` | Response headers the server adds, including your Content-Security-Policy. It's re-read on every request, so just reload the browser. | **Part 1** |
| `public/index.html` | The page | **Part 1** |
| `public/app.js` | Wiring: config, page state, and buttons connected to features | **Parts 1–4** |
| `public/render.js` | Everything that puts data on the screen | **Part 2** |
| `public/form.js` | Checks for the "Post an event" form | **Part 2** |
| `public/api.js` | The single network wrapper (a stub to start with) | **Part 3** |
| `public/safe-json.js` | Parsing, merging and schema tools (stubs to start with) | **Part 4** |
| `public/breakit.js` | The Break-it board that reports whether each attack worked | No |
| `public/styles.css` | Styles | No |
| `public/vendor/purify.min.js` | DOMPurify 3.4.12, served locally (no CDN) | No |

## Mock API

| Route | What it does |
|---|---|
| `POST /api/accounts` `{"username": "...", "password": "..."}` | Creates an account. Username: 3–20 letters, digits, `_` or `-`. Password: 8–72 characters. Returns `409` if the name is taken. |
| `POST /api/session` `{"username": "...", "password": "..."}` | Signs in and returns `{token, user}`, or `401` for a wrong username or password |
| `POST /api/session/expire` | Makes the server forget your token (simulates expiry) |
| `GET /api/events` | The event list. One event is hostile. |
| `POST /api/events` | Creates an event (the server validates too) |
| `GET /api/events/featured` | **Broken on purpose:** returns an HTML page with status 200 |
| `GET /api/events/:id` | One event, with its location. Event 4 is **broken on purpose.** |
| `GET /api/attendance` | **Slow on purpose:** takes 20 seconds to answer |
| `http://localhost:8081/partner/events` | Another origin with no CORS headers |
| `http://localhost:8081/frame.html` | Another site trying to frame Neighborly (clickjacking) |

Every route except `POST /api/accounts` and `POST /api/session` needs a token. Data, **including accounts**, lives in memory and resets when you restart the server, so create your account again after a restart. The server stores only a salted hash of each password and never logs it.

## Troubleshooting

| Problem | Fix |
|---|---|
| `EADDRINUSE` when starting | Something else is using port 8080 or 8081. Stop that program and start again. The demos (and the partner URL in `index.html`) are written for exactly these two ports, so please don't change them. |
| My `headers.json` change did nothing | Check that the file is valid JSON. The server prints a warning in its terminal if it isn't. Then reload the page. |
| The page is blank after enforcing the CSP | You still have an inline handler or inline script. Read the console and the server log: they give the file and line. |
| Old attacks still show after a fix | Rows stay flagged until you reload. Click **Reset demo** to clear saved data and reload. |
| Every button is dead and the console shows a `JSON.parse` error at start-up | You saved broken filter JSON (before Part 4 the app trusts saved data blindly). In the DevTools console run `localStorage.clear()`, then reload. |
