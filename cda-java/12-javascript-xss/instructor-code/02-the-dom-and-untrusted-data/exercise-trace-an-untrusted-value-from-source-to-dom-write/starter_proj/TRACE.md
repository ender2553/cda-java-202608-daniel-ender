# Trace Worksheet

Fill this in by reading `app.js`. Follow the greeting value from where it enters
JavaScript to where it is written into the page. Label each hop **data** or
**potential markup** based on *how the value will ultimately be written* — not on
whether the string looks dangerous.

## 1. Origin

- **Line / expression where the value first enters JS from outside:** `__________`
- **Untrusted source it came from** (URL/query param, form field, `localStorage`, server response): `__________`
- **Why is this source untrusted?** `__________`

## 2. Hops

| # | Variable / parameter | Expression | Label (data / potential markup) |
|---|----------------------|-----------|---------------------------------|
| 1 | `__________`         | `__________` | `__________` |
| 2 | `__________`         | `__________` | `__________` |
| 3 | `__________`         | `__________` | `__________` |

## 3. Crossing point

- **The exact DOM-write line:** `__________`
- **What the browser does with an attacker-supplied value written here:** `__________`
- **Why is this the line where `data` would cross into `markup`?** `__________`

## 4. Second entry point

- **The live input also feeds the same write. Where does that value enter, and what is its label?** `__________`
