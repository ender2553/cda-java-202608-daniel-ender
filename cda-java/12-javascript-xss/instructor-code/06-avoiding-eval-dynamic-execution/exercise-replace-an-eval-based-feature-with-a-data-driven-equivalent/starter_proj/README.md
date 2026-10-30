# Replace an eval-Based Feature with a Data-Driven Equivalent

## Your task

This page's "quick action" feature dispatches to a routine by running
`eval("do_" + action + "()")`, where `action` comes straight from the input box.

1. **Prove the vulnerability.** Open `index.html` in a browser and click **Run**. The input is
   pre-seeded with a benign exploit string (`save(); ...; //`) that closes the intended call and
   appends attacker code — you should see an `alert(...)` fire and the marker flip to **PAYLOAD
   FIRED**. That is arbitrary code running through the `eval` sink.
2. **Refactor to a lookup table.** In `app.js`, replace the `eval` dispatch in `run(action)` with a
   fixed object (or `Map`) that maps the known action names `save`, `load`, `export` to the
   developer-authored `do_*` functions.
3. **Validate the key.** Guard the dispatch with `Object.hasOwn(handlers, action)` so an
   unrecognized or hostile `action` selects nothing and nothing runs.
4. **Verify both directions.** Confirm the seeded payload now runs nothing (no alert, marker stays
   INERT) and the legitimate actions `save` / `load` / `export` still work.
5. **Check for hidden sinks.** Make sure your rewrite introduced no `new Function`, no
   string-argument `setTimeout` / `setInterval`, and no other dynamic-execution sink.

> **Contained demo.** The seeded payload only pops a local `alert(...)` and flips an on-page marker.
> It makes no network calls and touches no real site. Keep any payload you try equally benign.

## Files

- `index.html` — the Quick Action Console page: an action input (pre-seeded with a benign exploit),
  a Run button, an output log, and an attacker marker that shows INERT vs. PAYLOAD FIRED.
- `app.js` — the feature logic. Contains the vulnerable `run(action)` that dispatches via `eval`,
  the three `do_*` routines, and the marker helper the demo payload calls.

## Acceptance

- The starter demonstrates the vulnerability: running the seeded `action` executes injected code
  (alert fires, marker flips to PAYLOAD FIRED) through the `eval` sink.
- After your fix, `eval` is gone entirely — not wrapped in a filter or left on a fallback branch.
- Untrusted `action` becomes a **validated key** into a fixed handler table, never code.
- Dispatch is guarded with `Object.hasOwn(handlers, action)` (not a bare `handlers[action]`).
- The seeded exploit input now runs nothing (no alert, marker stays INERT); `save` / `load` /
  `export` still work.
- No other dynamic-execution sink (`new Function`, string-argument timers, `javascript:` URL,
  inline `on*` handler) was introduced.
