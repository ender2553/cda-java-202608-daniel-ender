# Trace Untrusted Input to Its Sinks

## Your task

Open `index.html` in a browser. Four panels each pull a value from a different
untrusted source — a form field, a URL query parameter, `localStorage`, and a
simulated API response — and write it into the page. Your job is to **trace
and label each path, not to fix it.** Read `app.js`, fill in every
`// TRACE:` blank, and complete `TRACE.md`: for each of the four panels, name
the source, follow the value through each hop labeling it **data** or
**potential markup**, identify the exact sink it lands in, and state the
defense that sink would need. Label by *how the value gets written*, not by
whether the string looks dangerous.

## Files

- `index.html` — the runnable four-panel page; open it in a browser (no setup, no server).
- `app.js` — reads each source and writes it to the DOM, with `// TRACE:` blanks for you to fill.
- `TRACE.md` — the worksheet where you record each source, its hops, its sink, and the required defense.

## Acceptance

- Every `// TRACE:` blank in `app.js` is filled and `TRACE.md` is complete.
- Each **origin** is correctly named as one of the four untrusted source types from the lesson (form field, URL query parameter, `localStorage`, API response), with a reason it counts as untrusted — including why the `localStorage` and API-response sources still count even though they don't "look" external.
- Each **hop** is labeled **potential markup**, consistent with the fact that every path in this file ends in a markup-parsing or attribute sink.
- Each **sink** is correctly named (`innerHTML`, `href` attribute assignment, `insertAdjacentHTML`) with a correct statement of what the browser does with an attacker-supplied value there.
- The **required defense** named for each sink matches the sink's context: output encoding for the welcome banner and profile link, and a note that the comment body needs a real sanitizer (not plain encoding) because it is meant to keep some formatting.
- Typing a payload like `<b>hi</b>` into the display-name field, or opening the page with `?returnTo=` set to a value containing `&`, demonstrates the same sink each panel's worksheet row describes.
