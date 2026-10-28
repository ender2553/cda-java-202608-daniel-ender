# Summative — Harden & Review the Static Site End to End

You are given a **working, cleanly linked multi-page site** (the skeleton from
the previous lesson): pages that reflow, links that resolve, a form that refuses
malformed input. Working is not the same as hardened. Your job is to turn this
working site into a **defensible** one and then prove it with an honest review.

Static HTML/CSS only — **no build system, no JavaScript.** Do not add any
`<script>`; if the strict policy breaks something, fix the source, never loosen
the policy to `'unsafe-inline'`.

## What you are given

```
index.html      Home     — working, NO policy applied yet
catalog.html    Catalog  — working, NO policy applied yet
contact.html    Contact  — working validated form, NO policy applied yet
css/site.css    Shared stylesheet (external — no inline styles anywhere)
images/logo.svg Site logo
```

## Task

1. **Apply the baseline.** Add a CSP `<meta http-equiv="Content-Security-Policy">`
   tag to *every* page and document the recommended response-header set (create
   a `_headers` file). Start from a restrictive `default-src 'self'` and loosen
   only to real needs — for example, the contact form posts to an external
   `https://` endpoint, so `form-action` must name that endpoint. Tune until
   every page loads with a clean dev-tools console.
2. **Validate and check.** Validate the markup on every page and fix errors;
   check landmarks, heading order, `alt` text, labeled controls, and focus
   order; check color contrast against readability thresholds.
3. **Re-test the form.** After hardening, confirm the form still rejects an empty
   required field and a malformed email and still submits via `POST` over HTTPS.
4. **Self-review.** Create a foundations security checklist (encoding, safe
   links, input constraints, policy baseline, transport, accessibility), score
   each item, name the threat each defends against, and remediate every failure
   before sign-off.
5. **Present.** Write a short walkthrough that maps at least the CSP, one
   header, and the input constraints to the threats they address.

## Acceptance

- Every page carries the CSP meta tag and loads with a clean console — no policy
  violations anywhere.
- An injected inline `<script>` is refused by the policy (no `'unsafe-inline'`).
- Markup validates; landmarks, heading order, `alt` text, labels, focus order,
  and contrast all pass.
- The form still rejects malformed input and posts over HTTPS after hardening.
- A completed checklist shows every item scored with any failure remediated, and
  you can name the threat each control defends against.

## Deliverables

- The three hardened pages (CSP meta tag on each).
- A `_headers` file documenting the response-header baseline.
- A completed security checklist (e.g. `SECURITY-CHECKLIST.md`).

## Boundary

This demonstrates the **client-side-defense foundations** — encoding, safe
links, input constraints, and a browser-enforced policy baseline. Runtime DOM
sanitization of untrusted data, and server-side validation, are beyond this
course's scope; client-side validation is a first line, never the only line.
