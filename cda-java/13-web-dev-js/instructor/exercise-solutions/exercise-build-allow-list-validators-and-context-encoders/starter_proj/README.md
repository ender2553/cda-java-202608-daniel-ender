# Build Allow-List Validators and Context Encoders

## Your task

Implement every function in `validators.js` — hand-written HTML-body,
attribute, and URL encoders, plus allow-list validators for structured
values (identifier, enum status, integer, date). No library. Each `// TODO:`
marks a function to complete:

- `encodeForHtml(str)` — HTML-entity encode `&`, `<`, `>`, `"`, `'` so a
  string is safe to write into the HTML body.
- `encodeForAttribute(str)` — the same encoding, applied at an attribute
  call site, so a quote-breakout payload cannot escape a `title="..."`
  attribute built by string concatenation.
- `encodeForUrl(str)` — encode a value for insertion into a URL's query
  string.
- `validateIdentifier(value)` — allow-list an identifier against
  `/^[a-z0-9_-]{3,32}$/`, throwing on anything that doesn't match.
- `validateStatus(value)` — allow-list a status against the exact set
  `{'open', 'in_progress', 'closed'}`.
- `validateInteger(value, {min, max})` — parse and range-check an integer.
- `validateDate(value)` — validate a strict `YYYY-MM-DD` string against a
  real calendar date (reject e.g. `2023-02-30`).

Every validator must **fail closed**: reject or throw on anything that
doesn't match, never pass a partially-trusted value through.
`stripTagsNaive` is provided complete — it's a documented *failure*
demonstration (LO5), never a defense; do not use it to sanitize anything.

Open `index.html` in a browser to test your work interactively, or click
**Run bypass-payload suite** to run a fixed hostile-payload set against every
field.

## Files

- `index.html` — the runnable demo page (open directly, no server).
- `validators.js` — every encoder/validator stubbed with `// TODO:` markers for you to complete.
- `app.js` — wires the validators to the page (complete; do not need to edit).

## Acceptance

- Typing `<script>alert(1)</script>` or `<img src=x onerror=alert(1)>` into **Display name** shows the tag as visible text in the preview — no alert, no DevTools console error, no live `<script>`/`<img>` element in the DOM.
- Typing `" onmouseover="alert(1)` or `"><img src=x onerror=alert(1)>` into **Bio** never breaks out of the `title` attribute — inspecting the rendered `<span>` in DevTools shows the payload entity-encoded inside the `title` value.
- Typing `javascript:alert(1)` or `x&admin=true` into **Search term** produces a percent-encoded query value — clicking the link runs no script and adds no unintended parameter.
- Typing `../../etc/passwd`, a non-enum status, a negative or 999 age, or `2023-02-30` into the structured fields shows `REJECTED`.
- Clicking **Run bypass-payload suite** shows **PASS** for all 13 rows, with zero `[bypass-suite] FAILED` lines in the DevTools console.
