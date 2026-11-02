# Write a Complete CSP for a Multi-Resource Page

## Your task

This sign-up page loads a stylesheet, a self-hosted font (`@font-face` in `styles.css`), a logo
image, a same-origin script, and submits via `fetch()` to a same-origin API route (`/api/echo`). It
currently ships with **no** Content Security Policy.

1. Start the server from this project root: `node ../../../../../../_server/serve.js`
2. Create a `headers.json` in this project root that names every resource type this page actually
   uses — `script-src`, `style-src`, `img-src`, `font-src`, `connect-src` — scoped to `'self'` (every
   resource here is same-origin).
3. Add the hardening trio: `object-src 'none'`, `base-uri 'self'`, `form-action 'self'`.
4. Do **not** use `'unsafe-inline'` or `'unsafe-eval'` anywhere in the policy.
5. Reload with DevTools Console open and confirm **zero** violations — every resource on this page
   is legitimate and same-origin, so a correctly scoped policy blocks nothing.

## Files

- `index.html` — the sign-up form (stylesheet, logo image, same-origin script, form submit via fetch).
- `styles.css` — includes a self-hosted `@font-face` rule (exercises `font-src`).
- `logo.svg`, `app-sans.woff2` — same-origin image and font assets.
- `app.js` — wires the form's `submit` to a same-origin `fetch('/api/echo', ...)` call.
- `headers.json` — **you create this.** Not present in the starter.

## Acceptance criteria

- `headers.json` sets `script-src`, `style-src`, `img-src`, `font-src`, and `connect-src` (each
  scoped to `'self'`), plus `object-src 'none'`, `base-uri 'self'`, `form-action 'self'`.
- No `'unsafe-inline'` or `'unsafe-eval'` anywhere in the policy string.
- Loading the page in a browser produces zero Console violations, and the stylesheet, logo, and
  script all load; submitting the form updates the status text.
