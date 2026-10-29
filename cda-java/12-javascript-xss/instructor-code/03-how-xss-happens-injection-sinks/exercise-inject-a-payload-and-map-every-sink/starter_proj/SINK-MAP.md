# Sink Map — Community Board demo

Fill this file in as you audit the page. Do **not** change `index.html` or `app.js` —
this exercise is a diagnosis, not a repair.

## Step 1 — Fire the injection

Type the benign payload below into the search box and submit it, then confirm the
`alert(1)` fires:

```
<img src=x onerror=alert(1)>
```

## Step 2 — Explain why it executed

In one or two sentences, describe the mechanism (parser builds a node, broken `src`,
`onerror` handler runs). Seeing the alert is not enough — name what happened.

> _Your explanation here._

## Step 3 — Map every sink

Audit **both** `index.html` and `app.js`. Find every API that parses a string as HTML:
`innerHTML`, `outerHTML`, `document.write`, `insertAdjacentHTML`. Look for sinks hidden
behind `+` concatenation or template literals, not just direct assignments. Record one
row per sink.

`Verdict` = **must-fix** if untrusted data (user input, URL, storage, or a server
response) reaches the sink, or **lower-risk** if the value is a developer-authored
constant. The trust boundary is the *data source*, not the sink alone.

| Location | Sink | Data source | Verdict |
|----------|------|-------------|---------|
|          |      |             |         |
|          |      |             |         |
|          |      |             |         |
|          |      |             |         |
|          |      |             |         |
|          |      |             |         |
|          |      |             |         |
