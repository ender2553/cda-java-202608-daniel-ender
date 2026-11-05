# Refactor to a Secure API-Client Module

## Your task

This dashboard has three widgets — Profile, Ping, and Fixture check — each
written with its own independent, duplicated `fetch()` call. Refactor them
into a single secure API-client module:

1. **Auth, base URL, and headers injected once**, in the wrapper. No widget
   should attach `Authorization` (or any other shared header) individually
   — remove all three hand-attached copies.
2. **Response validation enforced for every call**, wired into the wrapper
   itself. No call site should be able to skip it — not by an oversight
   (Profile, which currently validates nothing) and not by an inconsistent
   partial check (Ping, which only checks `response.ok`).
3. **Uniform timeout, retry, and error handling**: add a timeout via
   `AbortController`; retry only idempotent (`GET`) requests, with backoff,
   never retrying a request that already failed validation; produce **one**
   consistent error shape across network, HTTP, and validation failures —
   not three differently-shaped error strings like the current widgets
   produce.
4. **Check the wrapper's endpoint list against `connect-src-policy.json`**
   and flag any endpoint your wrapper knows about that the policy doesn't
   cover. Display the result under "Policy check."
5. **Confirm the hostile fixture is still handled safely through the new
   wrapper.** `fixtures/hostile-field.json` carries a payload in a
   correctly-typed field (same as the L14 exercise) — after your refactor,
   loading it through the wrapper must produce the same safe outcome L14
   did: the payload is never executed, no matter what path the data took to
   get to the page.

## Files

- `index.html` — the runnable dashboard page (three widgets + a Policy
  check area).
- `app.js` — the three scattered widget implementations; this is what you
  refactor.
- `connect-src-policy.json` — the page's supplied CSP `connect-src`
  allow-list.
- `fixtures/valid.json`, `fixtures/shape-invalid.json`,
  `fixtures/hostile-field.json` — the same three fixtures from the previous
  lesson's exercise, reused here to prove the wrapper's validation still
  holds when routed through a single module instead of a call-site check.

## Run it

From this folder:

```
node ../../../../../../_server/serve.js
```

Then open `http://localhost:8080`.

## Acceptance criteria

- `app.js` contains exactly one place that attaches the `Authorization`
  header, one place that decides the base URL, and one place that applies
  default headers — none of it duplicated per widget.
- Every one of the three widgets' requests is validated (content-type +
  shape) before its data is used, with no widget-specific opt-out.
- All three widgets report failures through the same error shape (check the
  console/UI — it should not read as three different error formats).
- The "Policy check" area reports at least one flagged mismatch between the
  wrapper's registered endpoints and `connect-src-policy.json` (the wrapper
  should register more than just the three endpoints the widgets call).
- Loading `fixtures/hostile-field.json` through the refactored path never
  executes its payload (confirm `window.__hostilePayloadFired` stays
  `undefined` in the console), matching the outcome from the L14 exercise.
