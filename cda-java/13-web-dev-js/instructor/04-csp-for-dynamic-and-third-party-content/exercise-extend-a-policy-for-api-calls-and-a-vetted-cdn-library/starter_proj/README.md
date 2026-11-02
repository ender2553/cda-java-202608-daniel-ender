# Extend a Policy for API Calls and a Vetted CDN Library

## Your task

`headers.json` currently authorizes only `'self'` for scripts and connections. This page needs to:

- load a vetted charting library from `https://cdn.example-charts.net/libs/charting/`, and
- call a partner API at `https://partner-api.example.com`.

It also (deliberately) attempts a call to an unvetted origin,
`https://sketchy-analytics.example`, which should **never** be added to the policy.

1. Start the server: `node ../../../../../../_server/serve.js`
2. Extend `script-src` to include the vetted CDN origin, **scoped to the specific path it serves
   from** (`https://cdn.example-charts.net/libs/charting/`) — not the whole host.
3. Extend `connect-src` to include the partner API origin (`https://partner-api.example.com`).
4. Leave the unvetted origin out of every directive.
5. Reload with Console open and confirm: the CDN script and partner API call now succeed, the
   same-origin `/api/echo` call still works (already covered by `'self'`), and the unvetted call is
   still blocked with a CSP violation.

## Files

- `index.html` — loads the vetted CDN script; boots `app.js`.
- `app.js` — makes three calls: same-origin (`/api/echo`), partner API, and the unvetted origin.
- `headers.json` — baseline policy, `'self'`-only. **You extend this.**

## Acceptance criteria

- `script-src` includes `https://cdn.example-charts.net/libs/charting/` (path-scoped, not a bare
  host or wildcard).
- `connect-src` includes `https://partner-api.example.com`.
- `https://sketchy-analytics.example` appears in **no** directive.
- The same-origin `/api/echo` call still returns 200 (nothing broke on the already-working path).
