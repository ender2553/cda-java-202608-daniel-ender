# Lesson 3 — Secure REST Endpoint Design: Headers, CSRF, CORS (STUDENT / RED)

This is the **starter** tree. It compiles, but the transport-layer tests **fail** (RED).
Your job is to configure `SecurityConfig` so they pass.

## The task

Edit `src/main/java/academy/rti/smc/SecurityConfig.java` (look for the
`// TODO: configure security headers + least-privilege CORS` marker) so that the
`SecurityFilterChain` adds the control introduced in this lesson:

1. **Security headers** — keep Spring Security's secure defaults (they already provide
   `X-Content-Type-Options: nosniff`) and **add** an explicit
   `Referrer-Policy: no-referrer` header. The default filter chain does *not* add a
   `Referrer-Policy`, so this is on you.
2. **Least-privilege CORS** — register a `CorsConfigurationSource` that allow-lists
   **exactly** `https://app.example.com` (plus the methods/headers the endpoint needs).
   The allowed origin must be echoed on a preflight; every other origin (e.g.
   `https://evil.example.com`) must **not** be granted CORS access.
3. **CSRF** — leave CSRF **disabled** (already done). See the CSRF decision below.

Do **not** add authentication — that is a **later lab**. The starter already permits all
requests, and it should stay that way.

## Why the tests are RED right now

The starter filter chain `permitAll`s and disables CSRF, but it:

- never registers a `CorsConfigurationSource`, so neither the allowed-origin echo nor the
  evil-origin rejection behaves correctly; and
- never adds the required `Referrer-Policy` header.

So three assertions in `SecureTransportTest` fail until you add the configuration:

- the `Referrer-Policy: no-referrer` header assertion on `GET /api/health`;
- the allowed-origin echo on the `https://app.example.com` preflight;
- (the evil-origin rejection assertion may pass incidentally, but only the full
  allow-list configuration makes the allowed-origin case correct).

The CSRF test (`POST /api/transactions` without a token → `201`) already passes — that
control is intentionally pre-configured.

## Pre-baked prior control

Bean Validation on the request DTO (`TransactionRequest`) is **carried forward** from the
prior lesson. It is not the focus here — leave it in place.

## CSRF decision (documented)

This API is a **stateless JSON API**: no server-side session, no cookie-based auth. The
classic CSRF vector depends on the browser auto-attaching an ambient credential (a session
cookie) to a forged cross-site request; with no such credential, that vector does not
apply. CSRF protection is therefore **deliberately disabled**, which is why a normal
`POST /api/transactions` succeeds without a CSRF token.

> If cookie/session-based authentication is added later, revisit this and re-enable CSRF.

## Run

```bash
mvn -q -B test
```

Requires JDK 21 (`JAVA_HOME` must point at a Java 21 runtime). Spring Boot 3.3.5.
Expect FAILURES until you complete the `SecurityConfig` TODO.
