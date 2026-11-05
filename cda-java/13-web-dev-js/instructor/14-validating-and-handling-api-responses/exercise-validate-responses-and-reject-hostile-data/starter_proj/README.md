# Validate Responses and Reject Hostile Data

## Your task

`app.js` loads four "orders": one from the live mock API's `/api/hostile`
route (which returns a non-JSON body), and three from local fixtures — a
valid order, a shape-invalid order (`total` arrives as a string instead of a
number), and one carrying a hostile field (`customer.name` contains markup,
but is still, technically, a string).

Run it as-is and see all three failure modes: an unhelpful raw parse error,
an uncaught `TypeError`, and — worst — the hostile markup actually executing
because it's rendered with `innerHTML`.

Fix `app.js` so every response is checked before it's used:

- **Check `Content-Type`** before calling `response.json()`. A non-JSON body
  should fail with a clear, specific error — not an opaque parse exception.
- **Validate shape and field types** before use: confirm `customer.name` is
  a string and `total` is a number before you use them. Reject anything that
  doesn't match, with a safe, generic failure message — never a partial
  render.
- **Never render with `innerHTML`.** Even a field that passes your shape
  check (right type, present) can still carry hostile content — render every
  field as text (`textContent` / `createTextNode`), never as markup. This is
  true even for the fixture that passes a naive shape check.

## Files

- `index.html` — the runnable order feed page.
- `app.js` — loads and renders the four sources; this is what you fix.
- `fixtures/valid.json` — a correctly shaped order.
- `fixtures/shape-invalid.json` — `total` is a string, not a number.
- `fixtures/hostile-field.json` — `customer.name` is the right type (a
  string) but carries a benign, local-only injection payload
  (`<img onerror=...>`) instead of a plain name.

## Run it

From this folder:

```
node ../../../../../../_server/serve.js
```

Then open `http://localhost:8080`.

## Acceptance criteria

- The live `/api/hostile` source fails with a clear, specific
  "expected JSON" message — never an opaque `SyntaxError` from a blind
  `response.json()` call.
- `fixtures/valid.json` renders correctly.
- `fixtures/shape-invalid.json` is rejected with a generic, safe failure
  message — never a raw `TypeError`, never a partial render.
- `fixtures/hostile-field.json` is rejected or rendered as **inert text**
  (the literal characters, not executed markup) — check
  `window.__hostilePayloadFired` in the console after the run; it must stay
  `undefined`.
- No source's failure crashes the loop — all four rows always finish
  rendering something.
