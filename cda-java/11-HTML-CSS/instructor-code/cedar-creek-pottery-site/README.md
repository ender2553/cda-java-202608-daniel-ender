# Cedar Creek Pottery — Secure Static Site

A finished three-page site covering both Module 5 code-alongs from *Introduction to HTML & CSS — Secure*:

1. **Building a Page From the Shared Skeleton**
2. **Applying a Restrictive Content-Security-Policy**

## Run it

The CSP uses `'self'`, so serve the folder over HTTP rather than opening files with `file://`:

```bash
cd cedar-creek-pottery-site
python -m http.server 8000
```

Open http://localhost:8000 with the dev tools console open.

## Files

```text
index.html               Home page
about.html               The code-along's About page, expanded
gallery.html             Image grid (shows img-src 'self' in use)
csp-violation-demo.html  Breaks the policy on purpose (not in the nav)
css/site.css             The one shared, mobile-first stylesheet
js/csp-monitor.js        The only JavaScript: shows CSP violations on the page
img/*.svg                Local images
_headers                 Response-header baseline (Netlify / Cloudflare Pages format)
```

## Where each code-along step lives

### Code-along 1: Building a Page From the Shared Skeleton

| Step | Where to see it |
|---|---|
| 1. Document metadata | The `<head>` of every page has the doctype, `lang`, charset, viewport, a descriptive title, and the stylesheet link. |
| 2. Semantic skeleton | Every page uses the same `header` > `nav aria-label="Primary"`, `main`, `footer` shape. |
| 3. Relative nav links | `href="index.html"` and the others. No leading slash and no domain. The current page is marked with `aria-current="page"`. |
| 4. Content, no inline styles | Each page has labelled sections and one `h1`. The pages have zero `style=` attributes. |
| 5. Safe external link | The footer guild link uses `target="_blank" rel="noopener noreferrer"`. |

### Code-along 2: Applying a Restrictive Content-Security-Policy

| Step | Where to see it |
|---|---|
| 1. CSP meta tag | Every page has the tag right after `charset`, before any resource it controls. |
| 2. Inline script is blocked | `csp-violation-demo.html` has `<script>console.log("inline test");</script>` before `</body>`. `"inline test"` never prints. |
| 3. Fix by removing, not loosening | The three real pages have no inline script. The demo page explains the correct fix. |
| 4. Name each source | The final policy: `script-src`, `style-src`, `img-src`, `object-src`, `base-uri`, and `form-action` are all named. |
| 5. Response-header baseline | In `_headers`, the CSP text matches the meta tag exactly, plus `X-Frame-Options`, `Strict-Transport-Security`, `X-Content-Type-Options`, and `Referrer-Policy`. |

## About the JavaScript

`js/csp-monitor.js` is the only script. It is an external file allowed by `script-src 'self'`, which shows learners why that directive exists. It listens for the browser's `securitypolicyviolation` event and:

- writes **"CSP: enforcing, 0 violations on this page"** in every footer, so you can see the policy holding across the whole site
- lists each blocked item on `csp-violation-demo.html`

The script is loaded in `<head>` without `defer` on purpose. The listener has to be registered before the browser parses the body. Otherwise it would miss violations caused by markup further down the page. It writes with `textContent` only and never uses `innerHTML`.

## Live demo of the violation (verified in Chrome)

Open `csp-violation-demo.html`. The browser blocks three things, and the page lists them:

```text
style-src-attr blocked inline style
img-src blocked https://images.example.com/teapot.png
script-src-elem blocked inline script
```

The "big and red" paragraph renders as plain text, and the console stays silent about `"inline test"`. To show the fix, delete the inline `<script>` and reload. The script violation goes away, and the policy is unchanged.

## Note: the meta tag can't do everything

A `<meta>` CSP can't set `frame-ancestors`, `X-Frame-Options`, HSTS, `nosniff`, or `Referrer-Policy`. Those only work as real response headers. That is why `_headers` exists. `python -m http.server` doesn't send them, so during local runs only the meta-tag CSP is enforced.
