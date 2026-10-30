# Rebuild a String-Concatenated Render as Safe DOM Construction

## Your task
You are given a working product page that renders a list of cards from a data array by concatenating an HTML string into `innerHTML`. Each card has a title and a "visit source" link. The data includes one entry whose title carries an `onerror` image payload and one whose link uses the `javascript:` scheme — open the page and you will see both fire (they write to the payload activity log). Rebuild the render so they cannot:

- Delete the string-concatenation render. Build each card with `document.createElement`, setting every visible field through `textContent`.
- Write an `isSafeUrl(value)` helper that parses the value with the `URL` constructor and checks `.protocol` against an allow-list of `https:`, `http:`, and `mailto:`; return `false` (fail closed) if the value will not parse.
- Assign `href`/`src` only after the value passes `isSafeUrl`. If a URL is rejected, leave the attribute unset — never assign a "cleaned" version.
- Attach any behavior with `addEventListener`, never through an inline event-handler attribute.
- Use `append` to attach nodes. Confirm no string is ever concatenated into markup anywhere in your rebuild.

## Files
- `index.html` — the runnable page: a rendered card region plus a payload activity log. Open it directly in a browser (no build, no server).
- `app.js` — the untrusted data array, the benign `reportXss` marker, and the **vulnerable** `renderCards` you will replace.

## Acceptance
- The `<img src=x onerror=...>` title renders as **inert, literal text** — the payload no longer fires (nothing new appears in the payload activity log on render).
- The `javascript:` link is **dropped** (no `href`, or the anchor is omitted) — clicking it does nothing.
- Every visible field is written with `textContent`; `isSafeUrl` parses with `new URL()` inside a `try/catch` and checks `.protocol` against the three-scheme allow-list.
- No `innerHTML` (or any string-built markup) remains on any data path; all behavior lives in `addEventListener`.
- All benign product cards still render, and their valid `https:` links still work.
