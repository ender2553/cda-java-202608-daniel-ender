# TRACE worksheet — Trace a Value to Its Source

For every value the script reads, record where it came from, where it flows, and
how it is classified. The first row is filled in as a worked model. Complete the
form-field row and answer the reflection questions.

| Value | Source | Stored in | Where it goes | Data or markup? | Trusted or untrusted? |
|---|---|---|---|---|---|
| `"Ledger"` | developer-authored literal | `const siteLabel` | `console.log` (not the page) | data (text) | trusted |
| URL `name` parameter | URL query string | `const nameParam` | `console.log` (not the page) | data (text) | untrusted |
| form field value | User-supplied form field (#nameField) | const formValue | console.log (not the page) | Data (text) |Untrusted |

## Reflection

1. What single question decides whether a value is trusted or untrusted?

Answer: Who controls the value or where did it originate? Developer-authored values are trusted within their intended context, while user-supplied values are untrusted.

2. The URL parameter looks like an ordinary clean string. Why is it still untrusted?

Answer: The user can change the URL parameter to contain arbitrary input. Its appearance doesn't establish that its contents are safe.

3. This script never writes any read value onto the page. Why is stopping at "read and log" the safe boundary for this exercise?

Answer: Reading and logging the values demonstrates how to trace their sources without interpreting them as HTML or inserting them into the page. This avoids introducing potential cross-site scripting (XSS) vulnerabilities through unsafe DOM manipulation.
