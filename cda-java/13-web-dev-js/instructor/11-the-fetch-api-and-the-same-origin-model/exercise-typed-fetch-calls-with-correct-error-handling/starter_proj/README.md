# Typed Fetch Calls with Correct Error Handling

## Your task

`app.js` calls four endpoints that represent the outcomes a real client has to
handle: a real success, a `404`, a `500`, and a genuinely unreachable URL. Run
it as-is first and see the bug: the `404` and the `500` cases both print as
"success," because their bodies still parse as valid JSON — `fetch()` only
rejects on a real network failure, never on an HTTP error status.

Fix `app.js` so each of the four outcomes is reported correctly:

- Wrap each request in `try`/`catch`, using `async`/`await`.
- Inside the `try` block, check `response.ok` **before** reading the body.
- Branch on `response.status` to report the `404` and `500` cases distinctly
  from each other, not just as "not ok."
- Only call `response.json()` once you've confirmed the response is the
  success case.
- Keep the `catch` block for genuine network failures (the unreachable URL) —
  it should report a different, distinct message than either HTTP-error case.

## Files

- `index.html` — the runnable dashboard page.
- `app.js` — calls the four endpoints and reports each outcome; this is what
  you fix.

## Run it

From this folder:

```
node ../../../../../../_server/serve.js
```

Then open `http://localhost:8080`.

## Acceptance criteria

Open the browser console and the Network panel, click **Run requests**, and
confirm:

- The `success (200)` row reports success with the actual response data.
- The `not found (404)` row reports a distinct HTTP-error message naming
  status `404` — not "success."
- The `server error (500)` row reports a distinct HTTP-error message naming
  status `500` — not "success."
- The `unreachable` row reports a distinct network-failure message (no HTTP
  status at all, since no response was ever received) — and it is not
  confused with either HTTP-error case.
- `response.json()` is never called on the `404` or `500` responses until
  after `response.ok` has been checked and found true.
