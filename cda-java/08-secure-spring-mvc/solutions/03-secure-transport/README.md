# Lesson 3 — Secure REST Endpoint Design: Headers, CSRF, CORS (INSTRUCTOR / GREEN)

This is the **solution** tree. All tests pass (GREEN).

## What this lab adds

The control introduced in this lesson is **transport-layer security**, configured in a
single `SecurityFilterChain` (`SecurityConfig`):

1. **Security headers** — Spring Security's secure defaults (which include
   `X-Content-Type-Options: nosniff`) **plus** an explicitly-configured
   `Referrer-Policy: no-referrer`. The custom `Referrer-Policy` is the graded header
   requirement, because the default filter chain does *not* add it on its own.
2. **Least-privilege CORS** — a `CorsConfigurationSource` allow-list of **exactly**
   `https://app.example.com`. On a CORS preflight the allowed origin is echoed back in
   `Access-Control-Allow-Origin`; any other origin (e.g. `https://evil.example.com`) is
   not granted CORS access.
3. **Deliberate CSRF stance** — CSRF protection is **disabled** (see decision below).

## Pre-baked prior control

Bean Validation on the request DTO (`TransactionRequest`) is **carried forward** from the
prior lesson. It is not the focus here; it is present in both trees so the behavior under
test is purely the transport configuration.

## Authentication is out of scope

Authentication is a **later lab**. To keep it out of scope, the filter chain authorizes
**all** requests (`anyRequest().permitAll()`). Only headers / CORS / CSRF differ between
the student and instructor trees.

## CSRF decision (documented)

This API is a **stateless JSON API**: there is no server-side session and no
cookie-based authentication. CSRF attacks rely on the browser automatically attaching an
ambient credential (typically a session cookie) to a forged cross-site request. With no
cookie/session credential to ride on, the classic CSRF vector does not apply, so CSRF
protection is **deliberately disabled** here. This is why a normal
`POST /api/transactions` succeeds **without** a CSRF token.

> If this API later adopts cookie/session-based authentication, this decision must be
> revisited and CSRF protection re-enabled (e.g. with the cookie-based token repository).

## Tests (identical in both trees)

`SecureTransportTest` (MockMvc):

- `GET /api/health` returns `X-Content-Type-Options: nosniff` **and** `Referrer-Policy: no-referrer`.
- CORS preflight (`OPTIONS /api/transactions`, `Origin: https://app.example.com`,
  `Access-Control-Request-Method: POST`) is allowed → `200` and
  `Access-Control-Allow-Origin: https://app.example.com`.
- CORS preflight with `Origin: https://evil.example.com` is **not** granted (no
  `Access-Control-Allow-Origin` header).
- `POST /api/transactions` with a valid body succeeds (`201`) **without** a CSRF token.

## Run

```bash
mvn -q -B test
```

Requires JDK 21 (`JAVA_HOME` must point at a Java 21 runtime). Spring Boot 3.3.5.
