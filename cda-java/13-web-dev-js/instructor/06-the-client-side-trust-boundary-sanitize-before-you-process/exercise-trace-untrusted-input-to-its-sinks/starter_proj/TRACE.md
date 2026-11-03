# Trace Worksheet — Four Sources, Four Sinks

## 1. Welcome banner (form field)

* **Origin (source type):** HTML form field (`#nameInput`), read through `input.value`. The fallback value is `SEEDED_NAME`.

* **Why is this source untrusted?** A user controls what they enter into the form. A submitted value can contain HTML or JavaScript-related payloads.

* **Hops (variable → label):**

  | Variable      | Label (data / potential markup) |
  | ------------- | ------------------------------- |
  | `input.value` | potential markup                |
  | `name`        | potential markup                |

* **Sink (exact line + DOM operation name):** `$('welcome').innerHTML = 'Welcome, ' + name + '!';` — `innerHTML` assignment.

* **What the browser does with an attacker-supplied value at this sink:** The browser parses the supplied string as HTML and creates DOM elements from it. An injected element with an event handler could execute JavaScript when its event occurs.

* **Required defense for this sink:** Output encoding appropriate for the HTML context, or use `textContent` to insert the name as plain text instead of parsing it as HTML.

## 2. Profile link (URL query parameter)

* **Origin (source type):** URL query parameter, read using `URLSearchParams` and `params.get('returnTo')`.

* **Why is this source untrusted?** Anyone can construct or modify a URL and supply an arbitrary `returnTo` value.

* **Hops (variable → label):**

  | Variable               | Label (data / potential markup) |
  | ---------------------- | ------------------------------- |
  | `fromUrl` → `returnTo` | potential markup                |

* **Sink (exact line + DOM operation name):** `link.href = '/profile?id=42&return=' + returnTo;` — assignment to the DOM element's `href` property (URL/attribute sink).

* **What could an unencoded `returnTo` value alter about the resulting URL?** Characters such as `&` can introduce additional query parameters, while `#` can introduce a URL fragment and change how the resulting URL is interpreted.

* **Required defense for this sink:** Validate the value and use `encodeURIComponent(returnTo)` when inserting it into the query-string value.

## 3. Theme badge (localStorage)

* **Origin (source type):** Browser `localStorage`, read using `localStorage.getItem('savedTheme')`.

* **Why is this source untrusted, even though nothing "typed" it just now?** Data in localStorage may have been stored earlier from an untrusted source. It can also be modified through browser developer tools or by other scripts running on the same origin. Stored data does not automatically become trusted.

* **Hops (variable → label):**

  | Variable | Label (data / potential markup) |
  | -------- | ------------------------------- |
  | `theme`  | potential markup                |

* **Sink (exact line + DOM operation name):** `host.insertAdjacentHTML('beforeend', '<strong>Theme: </strong>' + theme);` — `insertAdjacentHTML()`.

* **Required defense for this sink:** Output encoding before inserting the value into an HTML context, or use safe DOM methods such as `createElement()` and `textContent` instead of parsing the value as HTML.

## 4. Comment list (simulated API response)

* **Origin (source type):** Simulated API response returned by `fetchCommentsFromApi()`.

* **Why does the lesson still call API-response data untrusted, even though it "comes from our own backend"?** The backend may return information originally submitted by users or obtained from other untrusted sources. Receiving data from our own backend does not guarantee that the data is safe to insert into HTML.

* **Hops (variable → label):**

  | Variable       | Label (data / potential markup) |
  | -------------- | ------------------------------- |
  | `comment.body` | potential markup                |

* **Sink (exact line + DOM operation name):** `row.innerHTML = '<strong>' + comment.author + ':</strong> ' + comment.body;` — `innerHTML` assignment.

* **Required defense for this sink (name the specific technique — this field needs to keep some formatting, not become plain text):** Use an HTML sanitizer with a strict allowlist of permitted formatting elements and attributes. This allows approved formatting while removing dangerous HTML and event-handler attributes.

  **Additional observation:** `comment.author` is also inserted into the same `innerHTML` sink. It must be treated as untrusted and sanitized or safely rendered too.

## 5. Summary table

| # | Source                                  | Sink                   | Sink type          | Required defense                              |
| - | --------------------------------------- | ---------------------- | ------------------ | --------------------------------------------- |
| 1 | Form field (`input.value`)              | `innerHTML`            | HTML-parsing sink  | Output encoding or `textContent`              |
| 2 | URL query parameter (`returnTo`)        | `link.href`            | URL/attribute sink | Validate value and use `encodeURIComponent()` |
| 3 | `localStorage` (`savedTheme`)           | `insertAdjacentHTML()` | HTML-parsing sink  | Output encoding or safe DOM methods           |
| 4 | Simulated API response (`comment.body`) | `innerHTML`            | HTML-parsing sink  | HTML sanitizer with a strict allowlist        |
