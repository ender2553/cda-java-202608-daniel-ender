# Build the Secure Static-Site Skeleton

Integrative build: turn the separate skills you have practiced — a well-formed
document skeleton, semantic sectioning, responsive CSS, and a validated,
securely-submitting form — into **one** small multi-page static site.

This is a static HTML/CSS project. There is **no build system and no
JavaScript** — "runnable" means it opens in a browser and validates.

## What you are given

A partial shell, not an empty folder:

```
index.html      Home — shared header/nav/footer provided; <main> is yours to fill
catalog.html    Content page — shell only; build the sections
contact.html    Contact page — shell + a FORM STUB to finish wiring
css/site.css    Base chrome provided; layout/form/responsive rules marked TODO
images/logo.svg Site logo (referenced with alt text) — ready to use
```

Search the files for `TODO` to find every place you need to complete.

## Task

1. **Scope it.** Three pages sharing one header, one navigation block, one
   footer, and one stylesheet (already true of the shell — keep it that way).
2. **Build the pages.** Fill each `<main>` using the semantic skeleton
   (`<section>`/`<article>`, one `<h1>` per page, ordered headings). Reuse the
   provided shared header and footer on every page.
3. **Style once.** Complete `css/site.css` — main layout, the card row, the
   table, the form, and one `@media` responsive rule. No inline `style=` and no
   inline `<script>`.
4. **Embed the form.** Finish the contact form: labeled, correctly typed
   controls; the validation attributes (`required`, `type`, `minlength`,
   `maxlength`); and secure `method="post"` to an `https://` action.
5. **Link cleanly.** Every navigation link is relative and resolves; any
   `target="_blank"` link carries `rel="noopener noreferrer"`.

## Acceptance

- Every page has `<!DOCTYPE html>`, `<html lang="en">`, `<meta charset="utf-8">`,
  a viewport meta, and a descriptive `<title>`; every page validates.
- All three pages carry the identical shared header/footer and link the one
  shared stylesheet — no per-page CSS, no inline `style=`, no `<script>`.
- Resizing the browser reflows the layout at your media-query breakpoint.
- Every navigation and relative link resolves; the one new-tab link carries
  `rel="noopener noreferrer"`.
- The contact form has labeled, typed controls; keeps its validation
  attributes; and submits via `method="post"` to an `https://` action. An empty
  required field or a malformed email is refused by the browser before it sends.
