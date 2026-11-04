# Build a Reusable Sanitization Utility

## Your task

Complete `sanitize-utils.js` so `sanitizeObject()` consolidates this
module's techniques into one reusable utility that routes each field of a
nested "post submission" object to the correct treatment. Four functions are
stubbed with `// TODO:`:

1. **`safeUrl(rawUrl, base, { allowDataImages })`** — parse with the `URL`
   constructor, resolve relative URLs against `base`, allow only
   `http:`/`https:` by default. When `allowDataImages` is true, also allow a
   `data:` URL whose pathname starts with `image/` — a narrow,
   context-specific allowance, not a blanket one. Fail closed (return
   `null`) on everything else.
2. **`safeSetAttribute(el, name, value)`** — set the attribute through the
   DOM API, never string concatenation.
3. **`renderThenSanitizeMarkdown(markdownSource)`** — render the markdown
   source to HTML **first**, then sanitize the **rendered output** (not the
   source) with `DOMPurify.sanitize(html, MARKDOWN_SANITIZE_CONFIG)`.
4. **`sanitizeField(key, value, base)`** — route by field name: URL fields
   through `safeUrl`, the data-image field through `safeUrl` with
   `allowDataImages: true`, the rich-text field through
   `renderThenSanitizeMarkdown`, arrays by mapping each element back through
   `sanitizeField` with the same key, nested objects by recursing into
   `sanitizeObject`, and every other string through `encodeForHtml`.

`encodeForHtml` and `renderMarkdownRaw` are provided complete — you don't
need to edit them.

## Files

- `index.html` — the runnable demo page (open directly, no server).
- `purify.min.js` — DOMPurify 3.4.12, vendored locally (no CDN).
- `untrusted-submission.js` — one seeded nested "post submission" object (complete; do not need to edit).
- `sanitize-utils.js` — four functions to complete, marked `// TODO:`.
- `app.js` — sanitizes the submission and renders it (complete; do not need to edit).

## Acceptance

- Open `index.html`. The **Rendered (sanitized)** panel shows a display name, a "View profile" link, formatted body text, a tag list, and an author section — with no `alert`/`console.warn` firing from any payload.
- Because `profileUrl`'s only value is a `javascript:` payload, **View profile** must show no link at all — inspect the `<a>` in DevTools and confirm it has no `href` attribute.
- The **Sanitized submission** JSON panel shows `profileUrl` as an empty string, `displayName`/`meta.notes`/the payload tag entity-encoded, `bodyMarkdown` still containing `<strong>` formatting but no `<script>`/`<img>`/`javascript:`, `meta.authorUrl` resolved to an absolute `https://…` URL, and `meta.avatarDataUrl` preserved as a `data:image/png…` URL.
- The **Raw submission** panel shows the untrusted source unchanged (as plain text, never rendered as markup) for comparison.
