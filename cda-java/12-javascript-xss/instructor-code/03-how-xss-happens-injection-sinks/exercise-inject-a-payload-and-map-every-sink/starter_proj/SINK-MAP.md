# Sink Map — Community Board demo

**Contained training demo.** This page is deliberately vulnerable. This exercise is a diagnosis, not a repair. Do not change `index.html` or `app.js`.

## Step 1 — Fire the injection

Type the following benign payload into the search box and submit it:

```html
<img src=x onerror=alert(1)>
```

**Result:** Confirm that `alert(1)` fires when you submit the payload in the local training page.

## Step 2 — Explain why it executed

The browser parses the string assigned to `results.innerHTML` as HTML and creates an `<img>` element. Because `src=x` fails to load an image, the browser fires the element's `onerror` handler, which executes `alert(1)`; a `<script>` tag is not required.

## Step 3 — Map every sink

| Location                                  | Sink                 | Data source                                                                       | Verdict        |
| ----------------------------------------- | -------------------- | --------------------------------------------------------------------------------- | -------------- |
| `index.html` — inline script              | `document.write`     | Developer-authored constant containing the footer markup                          | **lower-risk** |
| `app.js` — greeting                       | `insertAdjacentHTML` | URL query parameter `name`, read into `nameFromUrl`                               | **must-fix**   |
| `app.js` — saved draft                    | `innerHTML`          | Browser `localStorage`, key `draft`, read into `savedDraft`                       | **must-fix**   |
| `app.js` — server message                 | `outerHTML`          | `serverMessage`, currently the developer-authored constant `'All systems normal'` | **lower-risk** |
| `app.js` — recent searches, initial clear | `innerHTML`          | Developer-authored empty string (`''`)                                            | **lower-risk** |
| `app.js` — recent searches, loop          | `innerHTML +=`       | User-influenced search terms stored in `localStorage`, key `recent`               | **must-fix**   |
| `app.js` — search results                 | `innerHTML`          | User-entered search-box value, stored in `query`                                  | **must-fix**   |

### Audit notes

* **`document.write`:** The inline script writes a developer-authored footer string during initial page parsing. The current value is a constant, so the verdict is `lower-risk`.
* **`insertAdjacentHTML`:** The URL-controlled name is concatenated into HTML before being inserted. Treat the URL as untrusted.
* **Saved draft:** Data retrieved from browser storage is not automatically trustworthy. Stored content can be modified.
* **Server message:** Although the code comment describes a stand-in for an untrusted server response, the current implementation uses the fixed string `'All systems normal'`. The verdict reflects the actual code. If the value came from a real untrusted server response, it would be `must-fix`.
* **Recent searches:** The initial assignment of `''` is a developer-authored constant. The later `innerHTML += row` uses markup assembled through string concatenation with stored search terms, so it is `must-fix`.
* **Search results:** The search-box value is concatenated into an HTML string and passed to `innerHTML`, allowing the demonstrated payload to execute.

**Conclusion:** The audit identified seven sink operations across `index.html` and `app.js`, including `document.write`, `insertAdjacentHTML`, `innerHTML`, `innerHTML +=`, and `outerHTML`. The verdicts are based on the source of the data reaching each sink, not just the API used.
