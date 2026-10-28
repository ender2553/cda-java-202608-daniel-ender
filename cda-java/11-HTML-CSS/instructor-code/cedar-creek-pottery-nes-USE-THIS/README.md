# Cedar Creek Pottery — NES.css Edition

A simple two-page version of the Module 5 code-alongs (*Building a Page From the Shared Skeleton* and *Applying a Restrictive Content-Security-Policy*). It loads its styles, its font and its images from other sites, so the CSP has to name each one.

## Run it

Open this folder in VS Code, right-click `index.html`, and choose **Open with Live Server**. Keep the dev tools console open.

## Files

```text
index.html    Home page: external images, NES.css sprites, button
about.html    The code-along's About page
css/site.css  Layout only. The retro look comes from NES.css.
js/site.js    Lists CSP violations in the footer
_headers      Response-header baseline. Its CSP matches the meta tag exactly.
```

## What the page loads from outside

| Resource | Comes from | Allowed by |
|---|---|---|
| NES.css stylesheet (pinned to 2.3.0, with `integrity`) | `https://unpkg.com` | `style-src` |
| Press Start 2P font CSS | `https://fonts.googleapis.com` | `style-src` |
| Press Start 2P font file | `https://fonts.gstatic.com` | `font-src` |
| Controller icon in the header | `https://nostalgic-css.github.io` | `img-src` |
| NES.css logo on the home page | `https://user-images.githubusercontent.com` | `img-src` |
| 10-Yard Fight and 1942 box art | `https://visionary-mochi-3391c5.netlify.app` | `img-src` |
| NES.css button and box borders (`data:image/svg+xml`) | inside nes.min.css | `img-src data:` |

The Mario, Kirby, Bulbasaur and Charmander sprites are drawn with CSS from NES.css, so they aren't image requests.

## Teaching points

- **Every external host is named on purpose.** Remove `https://unpkg.com` from `style-src` and reload: the whole retro look disappears, and the footer lists the violation.
- **The font needs two hosts.** Google's CSS comes from one host and the font file from another, so `font-src` needs its own rule.
- **`data:` in `img-src`** is there because NES.css draws its pixel borders with `data:` SVG images. Remove it and the buttons and boxes lose their borders. This is a good example of a framework's needs widening a policy. Make that choice knowingly.
- **`integrity` on the CDN link** means the browser refuses NES.css if unpkg ever serves a changed file. Google Fonts CSS changes for each browser, so it can't use `integrity`.
- **The "1 violation" is Live Server, not your page.** Live Server injects an inline `<script>` before `</body>` to make auto-reload work. `script-src 'self'` blocks it, so the footer shows `script-src-elem blocked inline code`. This is step 2 of the CSP code-along happening for real: a tool tried to add inline script, and the policy refused it. Auto-reload won't work, so press F5 after saving. Don't add `'unsafe-inline'` to get it back.

## Note: the meta tag can't do everything

`X-Frame-Options`, HSTS, `nosniff` and `Referrer-Policy` only work as response headers. That's why `_headers` exists. Live Server doesn't send them.
