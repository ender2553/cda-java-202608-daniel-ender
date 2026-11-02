# Refactor a Page to Pass a Nonce-Based Policy

## Your task

`headers.json` already applies a strict policy — `script-src 'self'`, no `'unsafe-inline'`. Under
that policy this page is currently **broken** in two ways:

1. **The two buttons don't work.** They're wired with `onclick="..."` attributes in `index.html`,
   which count as inline script and are blocked.
2. **The vendor analytics snippet never runs.** It's a small inline `<script>` block near the bottom
   of `index.html`. The vendor's integration requires it pasted inline, verbatim, on every page — it
   is not offered as an external file, so it can't simply be moved to `script.js`.

Fix the **application**, not the policy — do not add `'unsafe-inline'`.

1. Start the server: `node ../../../../../../_server/serve.js`. Load the page, open Console, and
   confirm both violations (one for each button's `onclick`, one for the inline analytics block).
2. Refactor the two `onclick` attributes: remove them from `index.html`, give each button an `id`
   (already present), and wire `submitFeedback` / `resetFeedback` with `addEventListener` inside
   `script.js`.
3. The analytics snippet genuinely cannot be externalized (vendor constraint) — it needs **nonce**
   authorization instead. This requires a per-response nonce, which a static `headers.json` cannot
   generate. Build a tiny dedicated server (see the solution for the reference pattern: a
   `server.js` that generates a fresh random nonce on every request, injects it into both the
   `Content-Security-Policy` response header and the `nonce="..."` attribute on the inline script
   tag via a template).
4. Verify: reload twice and confirm the nonce value in the response header is **different** on each
   request (a hardcoded, reused nonce defeats the whole mechanism — see the lesson's reflection
   question on exactly this mistake).

## Files

- `index.html` — feedback widget: two buttons wired via `onclick`, one vendor-inline analytics
  snippet, both currently blocked.
- `script.js` — `submitFeedback` / `resetFeedback` already defined and externalized; only the wiring
  needs to change.
- `headers.json` — the strict baseline policy (no nonce yet).

## Acceptance criteria

- No `onclick="..."` attributes remain in `index.html`; both buttons work via `addEventListener` in
  `script.js`.
- The vendor analytics snippet stays inline (unmodified content) but runs under a nonce — no
  `'unsafe-inline'` added anywhere.
- The policy's nonce value changes on every response (verified by comparing two consecutive
  requests).
- Console shows zero violations once the refactor is complete.
