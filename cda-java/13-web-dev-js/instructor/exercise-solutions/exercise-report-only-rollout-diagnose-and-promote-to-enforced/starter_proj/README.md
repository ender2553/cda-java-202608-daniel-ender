# Report-Only Rollout, Diagnose, and Promote to Enforced

## Your task

`headers.json` currently ships the target strict policy as
**`Content-Security-Policy-Report-Only`** — nothing is blocked yet, but every would-be violation is
reported to the shared server's `/csp-report` sink (which logs each report to its terminal) and, in
a real browser, to the DevTools Console as a report-only violation.

1. Start the server on the default port: `node ../../../../../../_server/serve.js`
2. Load the page, click **Refresh status**, and watch both the browser Console and the server's
   terminal output for `[csp-report]` lines.
3. **Diagnose**: trace the violation back to its root cause — which directive fired, which resource
   was involved, and which feature depends on it. (Hint: this page has one leftover piece of legacy
   markup that predates this app's `addEventListener` convention.)
4. **Fix the application, not the policy.** Do not add `'unsafe-inline'` or loosen anything in
   `headers.json` to make the report go away.
5. Once the fix is verified (zero reports on a fresh load + a refresh click), **promote**: change the
   header key in `headers.json` from `Content-Security-Policy-Report-Only` to
   `Content-Security-Policy` (still keeping `report-to` for ongoing monitoring — a fully-enforced
   policy that is working correctly still produces useful evidence when it blocks a real attack).

## Files

- `index.html` — order status page with one leftover `onclick` attribute (the bug).
- `app.js` — `legacyRefreshStatus` already externalized; only the wiring in `index.html` is broken.
- `headers.json` — report-only target policy, reporting to `/csp-report`.

## Acceptance criteria

- You can state the violation's full chain: directive (`script-src`) → resource (the inline
  `onclick` handler) → feature (`Refresh status` button).
- The fix touches `index.html`/`app.js` only — the policy's directive values are unchanged, only the
  header key moves from `-Report-Only` to enforced.
- After the fix, `headers.json` uses `Content-Security-Policy` (enforced), and a fresh page load plus
  a button click produce zero violations and zero `/csp-report` hits.
