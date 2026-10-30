# Secure a Vulnerable Page End-to-End

A small "Support Ticket Dashboard" works — it renders a ticket, formats a note,
follows a source link, computes a priority score, and applies saved settings —
but it is riddled with course-spanning flaws. All practice payloads are benign
and local: they only mark a row in the on-page **Attack Monitor** as *FIRED* and
log to the console. Nothing calls the network.

## Your task

Open `index.html` in a browser and watch the Attack Monitor: several payloads
fire on load and one link is armed to fire on click. Secure the page end-to-end
so every feature still works but no payload can execute:

- Render the **message** as text, not markup, so the `<img onerror>` displays
  literally instead of running.
- Sanitize the **rich-text note** with the bundled vetted library
  (`purify.min.js` / `DOMPurify`) using a minimal allow-list — keep `<b>`/`<a>`,
  drop the payloads. Do not hand-roll a filter.
- Allow-list the **source link** URL: accept only `http:`/`https:` and reject a
  `javascript:` URL before assigning `href`.
- Replace the **`eval`** priority feature with a data-driven lookup table that
  yields the same score.
- Harden the **JSON settings** path: reject `__proto__`/`constructor`/`prototype`
  keys, validate the shape, and never let a merge reach `Object.prototype`.
- Add positive **validation** at each input source (e.g. an enumerated status).

Then verify: re-run and confirm every Attack Monitor row stays *inert*, and be
ready to name which control neutralized each payload.

## Files

- `index.html` — the runnable dashboard page and the Attack Monitor.
- `app.js` — the page logic. Every flaw is marked `VULNERABLE`; this is what you fix.
- `untrusted.js` — the seeded untrusted input (message, note, URL, formula, settings JSON). Do not edit; treat it as data from outside your code.
- `purify.min.js` — a bundled, vetted sanitizer (DOMPurify) available for the note path. No install needed.

## Acceptance

- The message payload `<img src=x onerror=...>` renders as visible text and does not fire.
- The rich-text note shows `**urgent**` formatting and a safe link, but its `<img onerror>` and `javascript:` link are stripped by the vetted sanitizer.
- The source link resolves to a harmless value (not `javascript:`); clicking it runs nothing.
- No `eval`, `new Function`, string timer, `javascript:` URL, or inline handler remains in `app.js`; the priority score is unchanged.
- Parsing the settings JSON refuses the `__proto__` key: after load, `({}).isAdmin` is `undefined`, and the theme/page-size still apply.
- Every Attack Monitor row reads *inert*; you can name the control that stopped each payload.
