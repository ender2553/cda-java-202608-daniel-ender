# Inject a Payload and Map Every Sink

## Your task

You are given a small, sandboxed page that renders several values through DOM sinks. Open it in
a browser and:

- Type the benign payload `<img src=x onerror=alert(1)>` into the **search box**, submit it, and
  watch the `alert(1)` fire — proof that untrusted data written as markup becomes running script.
- In one or two sentences, explain *why* it executed (the parser builds an `<img>` node, its
  `src` of `x` fails to load, and the `onerror` handler runs — no `<script>` tag needed).
- Audit **both** `index.html` and `app.js` and find **every** dangerous sink: `innerHTML`,
  `outerHTML`, `document.write`, and `insertAdjacentHTML` — including any hidden behind `+`
  concatenation. Record each as a row in `SINK-MAP.md` with its location, sink, data source, and
  verdict (must-fix vs. lower-risk).
- Do **not** write a fix. The deliverable is the completed sink map — the diagnostic worklist.

Every exploit here is a harmless `alert(1)` against this local page only. Never run it against a
real or third-party site.

## Files

- `index.html` — the runnable demo page; includes one inline `<script>` sink of its own.
- `app.js` — the page's script; reads values from the URL, `localStorage`, and a seeded
  server-response string and renders them through several sinks.
- `SINK-MAP.md` — the worksheet you fill in: the injection explanation plus the sink-map table.

## Acceptance

- The payload `<img src=x onerror=alert(1)>` fires `alert(1)` when submitted through the search box.
- `SINK-MAP.md` explains the execution mechanically (node built, broken `src`, `onerror` fires),
  not merely "an alert appeared."
- The sink map lists every sink on the page, including at least one non-`innerHTML` sink and the
  sinks hidden behind `+` concatenation.
- Each row's verdict is defensible: sinks fed by untrusted sources are **must-fix**; sinks fed by
  developer-authored constants are **lower-risk**.
- No fix is written — `index.html` and `app.js` are unchanged.
