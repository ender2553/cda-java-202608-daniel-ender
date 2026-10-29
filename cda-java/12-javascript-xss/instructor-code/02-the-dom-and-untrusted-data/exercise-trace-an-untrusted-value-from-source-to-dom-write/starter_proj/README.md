# Trace an Untrusted Value from Source to DOM Write

## Your task

Open `index.html` in a browser and watch the greeting render — a seeded, attacker-style
value flows from an untrusted source into a DOM write, so you will see a benign
`alert('payload fired')` on load. Your job is to **trace and label that value, not to fix it.**
Read `app.js`, fill in every `// TRACE:` blank, and complete `TRACE.md`: name the untrusted
origin, follow the value through each hop labeling it **data** or **potential markup**, and mark
the single line where it would cross from data into markup. Label by *how the value gets written*,
not by whether the string looks dangerous.

## Files

- `index.html` — the runnable greeting page; open it in a browser (no setup, no server).
- `app.js` — the script that reads the value and writes it to the DOM, with `// TRACE:` blanks for you to fill.
- `TRACE.md` — the worksheet where you record the origin, each hop, and the crossing point.

## Acceptance

- Every `// TRACE:` blank in `app.js` is filled and `TRACE.md` is complete.
- The **origin** is correctly named as an untrusted source (the seeded stored value / URL `?name=` query parameter).
- Each **hop** (`fromUrl`/`name`, `rawName`, `greeting`) is labeled **potential markup**, consistent with the fact that it is written via `innerHTML`; the label reflects *how it is written*, not the string's appearance.
- The **crossing point** is identified as the `box.innerHTML = text` line, with a statement that an attacker value written there is parsed as markup and any embedded handler (e.g. `onerror`) executes in the page.
- The live input is identified as a second untrusted source (**form field**) feeding the same crossing point.
