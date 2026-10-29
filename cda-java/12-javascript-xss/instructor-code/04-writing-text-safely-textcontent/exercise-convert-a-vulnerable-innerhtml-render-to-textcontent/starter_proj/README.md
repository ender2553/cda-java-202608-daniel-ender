# Convert a Vulnerable innerHTML Render to textContent

## Your task

This guestbook page renders a visitor's display name and comment into the DOM with `innerHTML`, so a crafted value is parsed as markup and can run as script. First reproduce the injection so you see it fire, then secure the page:

- Open `index.html` in a browser, click **Load attack payload** (or type `<img src=x onerror="alert('xss')">` into a field), then **Post to guestbook** and confirm the alert fires — proof the value was parsed as markup.
- Find both `innerHTML` sinks in `app.js` (the name render and the comment render).
- Convert the **name** render to `textContent`, and rebuild the **comment** render with `document.createTextNode(...)` + `append`, so you practice both safe forms.
- Add a one-line comment on each converted line noting that the value traces to an untrusted source and must be rendered as text.
- Re-run the same payload and confirm it now appears as literal characters with no alert.

## Files

- `index.html` — the runnable guestbook page (name input, comment input, output card).
- `app.js` — reads the two untrusted inputs and renders them; currently uses `innerHTML` (the sinks you must fix).

## Acceptance

- Before the fix: entering `<img src=x onerror="alert('xss')">` and posting triggers the `alert('xss')` (the injection fires).
- This exploit is benign and local: an `alert()` against this page only — no network calls, no real site.

_When you finish the fix, the page should behave like `solution_proj/`: the same payload renders as visible text and does not execute._
