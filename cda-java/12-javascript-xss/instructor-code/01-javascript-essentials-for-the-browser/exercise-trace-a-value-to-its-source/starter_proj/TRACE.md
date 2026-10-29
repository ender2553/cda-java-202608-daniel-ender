# TRACE worksheet — Trace a Value to Its Source

For every value the script reads, record where it came from, where it flows, and
how it is classified. The first row is filled in as a worked model. Complete the
form-field row and answer the reflection questions.

| Value | Source | Stored in | Where it goes | Data or markup? | Trusted or untrusted? |
|---|---|---|---|---|---|
| `"Ledger"` | developer-authored literal | `const siteLabel` | `console.log` (not the page) | data (text) | trusted |
| URL `name` parameter | URL query string | `const nameParam` | `console.log` (not the page) | data (text) | untrusted |
| form field value | _fill this in_ | _fill this in_ | _fill this in_ | _fill this in_ | _fill this in_ |

## Reflection

1. What single question decides whether a value is trusted or untrusted?

   _Your answer:_

2. The URL parameter looks like an ordinary clean string. Why is it still untrusted?

   _Your answer:_

3. This script never writes any read value onto the page. Why is stopping at
   "read and log" the safe boundary for this exercise?

   _Your answer:_
