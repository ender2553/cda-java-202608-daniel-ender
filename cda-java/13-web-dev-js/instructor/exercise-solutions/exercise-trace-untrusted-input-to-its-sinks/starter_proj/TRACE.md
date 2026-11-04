# Trace Worksheet — Four Sources, Four Sinks

Fill this in by reading `app.js`. For each of the four panels, follow the value
from where it enters JavaScript to where it is written into the page. Label
each hop **data** or **potential markup** based on *how the value will
ultimately be written* — not on whether the string looks dangerous.

## 1. Welcome banner (form field)

- **Origin (source type):** `nameInput`
- **Why is this source untrusted?** `A payload could be delivered`
- **Hops (variable → label):**
  | Variable | Label (data / potential markup) |
  |---|---|
  | `input.Value___` | `_Markup_____` |
  | `name______` | `_Markup___` |
- **Sink (exact line + DOM operation name):** `innerHTML__`
- **What the browser does with an attacker-supplied value at this sink:** `Executes it directly`
- **Required defense for this sink:** `_output encoding__`

## 2. Profile link (URL query parameter)

- **Origin (source type):** `URL QUery Parameter_`
- **Why is this source untrusted?** `Anyone can modify the link`
- **Hops (variable → label):**
  | Variable | Label (data / potential markup) |
  |---|---|
  | `returnTo` | `Markup` |
- **Sink (exact line + DOM operation name):** `link.href = ...`
- **What could an unencoded `returnTo` value alter about the resulting URL?** `Introducing & or # can inject query params`
- **Required defense for this sink:** `URL Encoding`

## 3. Theme badge (localStorage)

- **Origin (source type):** `localStorage`
- **Why is this source untrusted, even though nothing "typed" it just now?** `If Storage is not sanitized or any attacker could alter the value`
- **Hops (variable → label):**
  | Variable | Label (data / potential markup) |
  |---|---|
  | `theme` | `Mrkup` |
- **Sink (exact line + DOM operation name):** `insertAdjacentHTML_`
- **Required defense for this sink:** `Output Encoding, text only values, createElement`

## 4. Comment list (simulated API response)

- **Origin (source type):** `API Response`
- **Why does the lesson still call API-response data untrusted, even though it "comes from our own backend"?** `API is not a trust guaruntee_`
- **Hops (variable → label):**
  | Variable | Label (data / potential markup) |
  |---|---|
  | `_comment.body_` | `Markup` |
- **Sink (exact line + DOM operation name):** `_row.innerHTML_`
- **Required defense for this sink** (name the specific technique — this field needs to keep *some* formatting, not become plain text): `HTML Sanitization, Allow List, DOMPurify`

## 5. Summary table

|  Summary table

| # | Source | Sink | Sink type | Required defense |
|---|--------|------|-----------|-------------------|
| 1 | Form field | `$('welcome').innerHTML = ...` | `innerHTML` | HTML-entity encoding (or `textContent`) |
| 2 | URL query parameter | `link.href = ...` | `href` attribute (string concat) | URL encoding (`encodeURIComponent`) |
| 3 | `localStorage` | `host.insertAdjacentHTML(...)` | `insertAdjacentHTML` | HTML-entity encoding |
| 4 | API response | `row.innerHTML = ...` | `innerHTML` | Parser-based sanitizer with a tight allow-list (DOMPurify