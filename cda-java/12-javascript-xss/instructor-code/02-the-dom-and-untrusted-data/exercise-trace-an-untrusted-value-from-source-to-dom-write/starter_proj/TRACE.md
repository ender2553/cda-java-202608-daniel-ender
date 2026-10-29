# Trace Worksheet

Fill this in by reading `app.js`. Follow the greeting value from where it enters
JavaScript to where it is written into the page. Label each hop **data** or
**potential markup** based on *how the value will ultimately be written* — not on
whether the string looks dangerous.

## 1. Origin

- **Line / expression where the value first enters JS from outside:** `const fromUrl = params.get('name');`
- **Untrusted source it came from** (URL/query param, form field, `localStorage`, server response): URL query parameter (`?name=`); if absent, the value falls back to the seeded `SEEDED_NAME`.
- **Why is this source untrusted?** A user can control the URL parameter, and the seeded value simulates attacker-influenced input. Neither value should be assumed safe.

## 2. Hops

| # | Variable / parameter | Expression                                                          | Label (data / potential markup) |
| - | -------------------- | ------------------------------------------------------------------- | ------------------------------- |
| 1 | `fromUrl` / `name`   | `params.get('name')` and `fromUrl !== null ? fromUrl : SEEDED_NAME` | potential markup                |
| 2 | `rawName`            | `const rawName = readNameFromSource();`                             | potential markup                |
| 3 | `greeting`           | `const greeting = 'Welcome, ' + rawName + '!';`                     | potential markup                |

## 3. Crossing point

- **The exact DOM-write line:** `box.innerHTML = text;`
- **What the browser does with an attacker-supplied value written here:**  The browser parses the assigned string as HTML markup. Attacker-controlled elements and event handlers, such as an `onerror` handler, may execute in the page.
- **Why is this the line where `data` would cross into `markup`?** The `innerHTML` property interprets the assigned string as HTML instead of treating it only as text. This makes it the point where untrusted data is interpreted as potential markup.

## 4. Second entry point

* **The live input also feeds the same write. Where does that value enter, and what is its label?** The value enters at `const typed = event.target.value;` in the form field's `input` event listener. Its source is the user-supplied form field, and its label is **potential markup** because it is passed through `render()` to the same `box.innerHTML = text;` write.
