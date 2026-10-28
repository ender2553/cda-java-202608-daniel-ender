# Riverside Books — Security Checklist

## 1. Project Overview

**Project:** Riverside Books — Harden & Review the Static Site End to End

**Purpose:** Apply and review client-side security protections for a multi-page static website.

**Technology:** HTML5 and CSS3 only. No JavaScript or build system.

### Project Files

```text
riverside-books/
├── index.html
├── catalog.html
├── contact.html
├── _headers
├── SECURITY-CHECKLIST.md
├── css/
│   └── site.css
└── images/
    └── logo.svg
```

## 2. Scoring System

Each security control receives a score:

* **0 — Not implemented or not verified:** The control is missing, fails, or lacks sufficient verification.
* **1 — Partially implemented:** The control is configured or partially tested, but its behavior or effectiveness has not been fully established.
* **2 — Implemented and verified:** The control has been tested, and its expected behavior has been confirmed.

A configured security control must not automatically receive a score of 2. Verification evidence is required.

## 3. Security Controls and Threats

| Security control              | Threat addressed                                                                     | Score | Status and evidence                                                                                                                                                    |
| ----------------------------- | ------------------------------------------------------------------------------------ | ----: | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Character encoding            | Misinterpreted characters and encoding inconsistencies                               |     2 | UTF-8 encoding is specified in the HTML documents.                                                                                                                     |
| Safe external links           | Untrusted linked pages gaining access to the originating browsing context            |     2 | External links using `target="_blank"` include `rel="noopener noreferrer"`.                                                                                            |
| Content Security Policy (CSP) | Unauthorized script execution and loading of unapproved resources                    |     1 | CSP meta tags are present on all three pages. Browser testing identified an inline-script violation associated with Live Server; the source still needs investigation. |
| Shared external stylesheet    | Unnecessary inline styles and inconsistent presentation                              |     2 | Pages reference the shared `css/site.css` stylesheet.                                                                                                                  |
| Image alternative text        | Information becoming inaccessible to users who cannot see images                     |     2 | The Riverside Books logo has alternative text.                                                                                                                         |
| Semantic landmarks            | Difficult navigation for assistive-technology users                                  |     2 | Landmark and page-structure checks passed.                                                                                                                             |
| Heading hierarchy             | Confusing document structure and navigation                                          |     2 | Heading order was checked and passed.                                                                                                                                  |
| Form labels                   | Form controls being difficult to identify and operate                                |     2 | Form labels and their associated controls were checked and passed.                                                                                                     |
| Keyboard focus order          | Keyboard users being unable to navigate the interface effectively                    |     2 | Keyboard navigation and visible focus were checked and passed.                                                                                                         |
| Color contrast                | Text and controls being difficult to perceive                                        |     2 | Color contrast checks passed against the applicable WCAG AA thresholds.                                                                                                |
| Input constraints             | Malformed or unexpected input reaching the form endpoint                             |     2 | Required-field, email-format, and topic-pattern tests passed. These client-side checks do not replace server-side validation.                                          |
| HTTPS form submission         | Form data being transmitted without transport encryption                             |     2 | Successful test submission was confirmed by the user. The HTTPS request and POST method should be recorded from browser Network evidence if formal proof is required.  |
| HTTP security headers         | MIME confusion, clickjacking, referrer leakage, and unnecessary browser capabilities |     1 | Recommended headers are documented in `_headers`. Actual delivery by a hosting server has not been verified.                                                           |
| HSTS delivery                 | Downgrade attacks and insecure subsequent connections                                |     1 | An HSTS header is documented in `_headers`, but delivery over HTTPS has not been verified.                                                                             |
| Form endpoint configuration   | Form submissions being sent to an unavailable or unintended destination              |     1 | A successful test was reported. Ensure the final HTML uses the actual working endpoint rather than the original placeholder URL.                                       |
| HTML validity                 | Parsing problems and inconsistent browser interpretation                             |     2 | All three pages passed the Nu HTML Checker without HTML errors. CSP-related warnings were reported separately.                                                         |

## 4. Content Security Policy

All three HTML pages contain a CSP meta tag.

The intended restrictive baseline is:

```html
<meta http-equiv="Content-Security-Policy"
      content="default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; font-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self';">
```

### Security Purpose

* `default-src 'self'` restricts resource loading to the site's own origin unless another directive specifies otherwise.
* `script-src 'self'` blocks inline scripts and restricts external script sources.
* `style-src 'self'` restricts stylesheet sources.
* `img-src 'self'` restricts image sources.
* `font-src 'self'` restricts font sources.
* `connect-src 'self'` restricts script-initiated network connections.
* `object-src 'none'` blocks embedded plugin content.
* `base-uri 'self'` restricts the URLs that can be used as the document's base.
* `form-action 'self'` restricts form submissions to the same origin.

**Important:** If the working contact form submits to an external origin, `form-action` must explicitly allow that trusted destination. The policy must match the final form configuration.

The policy must not be weakened by adding `'unsafe-inline'`.

### CSP Verification Record

| Test                                          | Result                      |
| --------------------------------------------- | --------------------------- |
| CSP meta tag on `index.html`                  | Present                     |
| CSP meta tag on `catalog.html`                | Present                     |
| CSP meta tag on `contact.html`                | Present                     |
| Inline-script execution blocked               | Observed in browser console |
| Inline-script source identified               | Pending                     |
| Live Server console free of CSP violations    | Not yet achieved            |
| Stylesheet and logo CSP warnings investigated | Pending                     |

The inline-script error disappeared when the page was opened directly from the filesystem and returned when Live Server was enabled. This suggests a difference in the testing environment, but the source has not been conclusively identified.

The Nu HTML Checker also reported CSP-related warnings involving the stylesheet and logo. These warnings require investigation; they do not, by themselves, establish whether the resources are blocked in the deployed website.

## 5. Recommended HTTP Security Headers

The root `_headers` file documents the recommended response-header baseline:

```text
/*
  X-Content-Type-Options: nosniff
  X-Frame-Options: DENY
  Referrer-Policy: strict-origin-when-cross-origin
  Permissions-Policy: camera=(), microphone=(), geolocation=()
  Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; font-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'
  Strict-Transport-Security: max-age=31536000
```

### Header Threat Mapping

| Header                                             | Threat addressed                                                       |
| -------------------------------------------------- | ---------------------------------------------------------------------- |
| `X-Content-Type-Options: nosniff`                  | Reduces MIME-type confusion and content-sniffing risks.                |
| `X-Frame-Options: DENY`                            | Helps prevent clickjacking by disallowing framing.                     |
| `Referrer-Policy: strict-origin-when-cross-origin` | Limits referrer information disclosed to other origins.                |
| `Permissions-Policy`                               | Restricts access to browser features that the site does not need.      |
| `Content-Security-Policy`                          | Restricts resource loading and helps defend against script injection.  |
| `Strict-Transport-Security`                        | Instructs supporting browsers to use HTTPS for subsequent connections. |

**Deployment limitation:** The `_headers` file does not activate response headers by itself. The hosting platform must support this format. Actual response headers must be inspected after deployment.

HSTS must only be delivered over HTTPS. The HSTS configuration should be verified against the deployed site's HTTPS response before this control receives a passing score.

The CSP response header and CSP meta tag should remain consistent. The response header can enforce additional restrictions, including `frame-ancestors`, which cannot be delivered through a CSP meta tag.

## 6. Form Input and Submission Tests

### Browser Validation

| Test                            | Expected result                                             | Result                                      |
| ------------------------------- | ----------------------------------------------------------- | ------------------------------------------- |
| Empty required fields           | Browser prevents submission and identifies a required field | Passed                                      |
| Malformed email address         | Browser prevents submission                                 | Passed                                      |
| Valid topic: `Book Order`       | Accepted                                                    | Passed                                      |
| Valid topic: `Reading, Event`   | Accepted                                                    | Passed                                      |
| Valid topic: `Book-Question`    | Accepted                                                    | Passed                                      |
| Invalid topic: `Book@Order`     | Rejected by the topic pattern                               | Passed                                      |
| Invalid topic: `Book#1`         | Rejected by the topic pattern                               | Passed                                      |
| Empty optional topic field      | Accepted                                                    | Passed                                      |
| Successful test submission      | Message reaches the configured endpoint                     | Confirmed by user                           |
| HTTPS transport and POST method | Request uses HTTPS and POST                                 | Record browser Network evidence if required |

### Form Security Considerations

The form's browser-side validation is a first line of defense. It does not guarantee that a server will accept only valid input.

A production implementation must validate submitted data on the server as well.

The final form action must point to a real, trusted endpoint. The original placeholder URL must not remain in the final implementation if the form is intended to deliver messages.

The CSP `form-action` directive must permit the final endpoint while restricting submissions to other destinations.

## 7. Accessibility and HTML Validation

All three HTML pages passed the Nu HTML Checker without HTML errors.

The following accessibility checks were also confirmed as passing:

| Requirement                         | Result |
| ----------------------------------- | ------ |
| Semantic landmarks                  | Passed |
| Logical heading order               | Passed |
| Image alternative text              | Passed |
| Associated form labels              | Passed |
| Keyboard focus order and visibility | Passed |
| WCAG AA color contrast              | Passed |

For color contrast, WCAG AA generally requires:

* **4.5:1** for normal text.
* **3:1** for large text.

Accessibility checks should cover the actual text and background combinations used by the site, including link and hover states where applicable.

### Validation Note

The Nu HTML Checker reported no HTML errors for the three pages, but it did report CSP-related warnings involving the stylesheet and logo.

Those warnings remain part of the CSP investigation and should not be described as resolved solely because the HTML itself validates.

## 8. Remaining Actions

The following items remain open:

* [ ] Identify the source of the Live Server inline-script CSP violation.
* [ ] Resolve the CSP issue without adding `'unsafe-inline'`.
* [ ] Investigate the stylesheet and logo CSP warnings.
* [ ] Confirm the final contact form uses its actual working endpoint.
* [ ] Capture or record evidence that the successful form request uses HTTPS and POST, if required for formal sign-off.
* [ ] Deploy to a compatible environment if actual HTTP response-header verification is required.
* [ ] Inspect the deployed response headers.
* [ ] Verify HSTS delivery over HTTPS.
* [ ] Update the relevant scores after the remaining checks pass.

## 9. Final Review

The Riverside Books website has made substantial progress toward the client-side security baseline.

**Completed and verified:**

* HTML validation for all three pages.
* Accessibility checks.
* Browser-side form validation.
* Successful form submission, as confirmed by the user.
* Presence of CSP meta tags on all three pages.
* Documentation of the recommended HTTP security headers.

**Still requiring verification:**

* A clean developer console under the intended testing environment.
* The source of the Live Server inline-script violation.
* The stylesheet and logo CSP warnings.
* Actual HTTP security-header delivery.
* HSTS delivery over HTTPS.

The project should receive final sign-off only after the outstanding acceptance criteria have been resolved or any genuine environmental limitations have been clearly documented.

This exercise demonstrates client-side security foundations. It does not replace server-side validation, production security testing, or a complete application security review.
