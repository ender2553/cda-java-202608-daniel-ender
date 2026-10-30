# Parse, Detect, and Harden an Untrusted JSON Payload

## Your task

You are given a small page that parses an untrusted JSON payload, merges it into
application state, and renders it — and it does all three unsafely. Open
`index.html` in a browser and work the exercise:

- Confirm the script parses with `JSON.parse` (not `eval` / `new Function`), and wrap the parse in `try/catch`.
- Load the hostile payload and watch the prototype-pollution probe flip `({}).polluted` to `true` on a brand-new empty object.
- Harden the object handling in `app.js`: validate the expected shape and reject mismatches, reject/strip the keys `__proto__`, `constructor`, and `prototype`, build the untrusted map with `Object.create(null)`, and read fields with `Object.hasOwn` instead of `key in`.
- Re-test: the hostile payload must no longer pollute, and the benign payload must still render.
- Name the render debt: identify where a parsed value is written to the DOM and switch that write to `textContent` (or safe node construction) so an embedded payload cannot execute.

All exploits here are benign and local (a visible on-page marker plus a console message) — nothing contacts the network.

## Files

- `index.html` — the runnable page: three payload buttons, a rendered-profile region, a prototype-pollution probe, and an XSS marker.
- `app.js` — the vulnerable script: safe parser but a naive recursive `unsafeMerge` (the pollution sink) and an `innerHTML` render (the XSS sink), with `TODO` markers where you harden.

## Acceptance

- Loading the hostile `__proto__` payload sets `({}).polluted` to `true` (pollution reproduced) before hardening.
- Loading the valid-shape payload whose `bio` is `<img src=x onerror=window.__xssFired()>` fires the XSS marker before hardening.
- This starter demonstrates the vulnerable/incomplete state — it is not yet hardened; that is your task.
- Runnable by opening `index.html` directly in a browser, with no build step or server.
