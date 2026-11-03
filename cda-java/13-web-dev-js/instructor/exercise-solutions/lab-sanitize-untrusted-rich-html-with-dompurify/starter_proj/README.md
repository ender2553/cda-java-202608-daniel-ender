# Sanitize Untrusted Rich HTML with DOMPurify

## Your task

Six seeded "notes" — untrusted rich text, some carrying attack payloads — are
already run through `DOMPurify.sanitize()` in `app.js`, but the configuration
is currently empty (DOMPurify's broad defaults) and there is no hook forcing
safe link attributes. Your job:

1. Scope `NOTE_SANITIZE_CONFIG` to the minimum this feature needs:
   `ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'a']` and
   `ALLOWED_ATTR: ['href']`.
2. Add a `DOMPurify.addHook('afterSanitizeAttributes', ...)` call that forces
   every surviving `<a>` element to carry `rel="noopener noreferrer"` (and,
   optionally, `target="_blank"`).

DOMPurify already strips `<script>` tags and `on*` handler attributes by
default — that part works even with an empty config. What tightening the
config buys you is scoping out everything else this feature doesn't need
(like the `<table>` in the last note), and the hook adds a constraint the
tag/attribute list alone can't express.

## Files

- `index.html` — the runnable note-viewer page (open directly, no server).
- `purify.min.js` — DOMPurify 3.4.12, vendored locally (no CDN).
- `untrusted.js` — six seeded untrusted notes (complete; do not need to edit).
- `app.js` — the sanitize call and render loop; `NOTE_SANITIZE_CONFIG` and the hook are your two `// TODO:` markers.

## Acceptance

- Open `index.html`: every note's **Raw** panel shows the untrusted source as plain text; every note's **Rendered** panel shows the sanitized output.
- Inspect each "Rendered" panel's DOM in DevTools: no `<script>` element, no `on*` attribute, no `javascript:` URL, and — once your config is scoped — no `<table>`/`<tr>`/`<td>` from the disallowed-tag note.
- The **legitimate formatting** note (`note-formatted`) still shows bold, italic, and emphasized text, and its link is clickable and points at `https://example.com/docs`.
- The **mixed** note (`note-mixed`) keeps its `<strong>` text and its link while its `onerror` payload is gone.
- Once your hook is added, every rendered `<a>` element has `rel="noopener noreferrer"` set (inspect in DevTools) — it should not be present before you add the hook.
