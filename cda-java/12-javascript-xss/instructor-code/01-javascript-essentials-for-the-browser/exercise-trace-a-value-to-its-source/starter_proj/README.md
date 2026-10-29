# Trace a Value to Its Source

## Your task

This local page reads values from input sources and logs them to the console —
it never renders them to the page, which keeps you at the safe boundary this
exercise is built around. The URL read is already wired up as a worked model.
Finish the second read from the form field, guard it with a strict
presence-and-type check, and label every value — in both `app.js` and
`TRACE.md` — as developer-authored (trusted) or user-supplied (untrusted),
naming its source.

## Files

- `index.html` — the runnable page: a form field, a button, and a status line. Open it in a browser, then open DevTools to watch the console.
- `app.js` — the trace script. The URL read is complete; the form-field read has a `// TODO` for you to finish, and each `// TRACE:` line needs its labels.
- `TRACE.md` — a worksheet with one row per value: its source, where it flows, and whether it is data/markup and trusted/untrusted.

## Acceptance

- Opening `index.html` in a browser (no build step) logs the developer-authored label and the URL read to the console on load.
- The script reads at least two values: the URL `name` parameter and the form field's typed value.
- Each read is guarded with a strict-equality presence-and-type check (`!== null`, `typeof … === "string"`) so a missing or wrong-typed value is handled, not assumed.
- Output is composed with a template literal and sent to `console.log`.
- Every value is annotated with its source and a trusted/untrusted label in both `app.js` and `TRACE.md`.
- The script writes none of the read values to the page — it only reads and logs.
