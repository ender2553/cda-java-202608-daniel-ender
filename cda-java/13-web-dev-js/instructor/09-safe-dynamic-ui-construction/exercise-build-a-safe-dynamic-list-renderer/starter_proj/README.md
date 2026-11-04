# Build a Safe Dynamic List Renderer

## Your task

`list-renderer.js` renders a directory list from `DIRECTORY_RECORDS`, but it
currently builds each record by string-concatenating an HTML string and
assigning it via `innerHTML`, and it drops every URL into `href` with no
scheme check. Rebuild it:

1. **`safeUrlOrNull(rawUrl, base)`** — parse `rawUrl` with the `URL`
   constructor (never a string-prefix check), resolving relative URLs
   against `base`. Return the resolved URL only if its scheme is `http:` or
   `https:`; return `null` for anything else — including anything that
   fails to parse.
2. **`createRecordListItem(record, base)`** — rebuild this using
   `createElement`, `setAttribute`, and `textContent` only. `record.name`
   and `record.bio` must render as literal text no matter what they
   contain. Only call `setAttribute('href', ...)` with a value
   `safeUrlOrNull` approved; if it returns `null`, render the record with
   **no link at all** (e.g. a `<span>` holding the name) rather than an
   unsafe `href`.

`renderList` (the collection loop) is already correct — no need to edit it.

## Files

- `index.html` — the runnable directory page (open directly, no server).
- `records.js` — six seeded untrusted directory records (complete; do not need to edit).
- `list-renderer.js` — `safeUrlOrNull` and `createRecordListItem` are your two functions to rebuild.
- `app.js` — calls `renderList(DIRECTORY_RECORDS, ...)` on load (complete; do not need to edit).

## Acceptance

- Open `index.html`: six records render. Record 2's name (`<img src=x onerror=...>`) must show as **visible literal text** — inspect it in DevTools and confirm there is no `<img>` element anywhere in that list item's subtree, and no console warning fires from it.
- Record 3 (`javascript:` URL) and record 4 (`data:` URL) must render with **no `<a>` element** at all — no clickable, executable link.
- Record 1 (a plain `https:` URL) and record 5 (a relative path) must render **with** a working, clickable link.
- Record 6's URL (containing an embedded `"` and `onmouseover=`) must still render a link — inspect its `<a>` element in DevTools and confirm it has **no `onmouseover` attribute**.
- View the DOM: no `innerHTML` assignment should remain anywhere in `list-renderer.js`.
