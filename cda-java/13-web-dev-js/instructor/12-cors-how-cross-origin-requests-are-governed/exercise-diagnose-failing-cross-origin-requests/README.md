# Diagnose Failing Cross-Origin Requests

## Pairing exception

**This activity is fixture-only.** Per the Module 3 code-manifest gate
decision, it ships no `starter_proj`/`solution_proj` pair — CORS failures are
a browser/server negotiation that is far more reliably taught from a fixed,
gradeable capture than from a live two-origin server setup. Instead it ships:

- `fixtures/` — four static console-error + Network-panel captures, each a
  realistic failing cross-origin request.
- `WORKSHEET.md` — the learner-facing diagnosis worksheet (classify each
  case, cite the evidence, state the fix).
- `ANSWERS.md` — the instructor-only answer key (**not** shipped to the
  student tree).

## Your task

Work through `WORKSHEET.md` against the four fixtures in `fixtures/`. For
each case, classify the failure, cite the specific header evidence from the
capture, and state the correct fix — always a server-side change, never a
client-side workaround like disabling CORS or adding a proxy.

## Files

- `fixtures/case-1-missing-allow-origin.txt` — a simple request with no
  `Access-Control-Allow-Origin` on the response at all.
- `fixtures/case-2-failed-preflight.txt` — a preflighted `POST` whose
  preflight response is missing `Authorization` from
  `Access-Control-Allow-Headers`.
- `fixtures/case-3-credentials-wildcard-conflict.txt` — a credentialed
  request against a response using the wildcard `Access-Control-Allow-Origin:
  *`, which credentials can never be combined with.
- `fixtures/case-4-404-not-cors.txt` — a URL typo that returns a genuine
  `404`, where the console still shows a CORS-shaped message because the
  server's 404 handler doesn't set CORS headers either.
- `WORKSHEET.md` — your diagnosis worksheet.
- `ANSWERS.md` — instructor answer key.

## Acceptance criteria

- Cases 1–3 are each correctly classified as a genuine CORS failure, with the
  specific missing/mismatched header named as evidence.
- Case 4 is correctly identified as **not** a CORS problem — a `404` from a
  URL typo — with an explanation of why the console message is misleading
  anyway.
- Every stated fix for a genuine CORS case (1–3) is a server-side change.
  None proposes disabling CORS, installing a "CORS unblock" extension, or
  adding a proxy.
