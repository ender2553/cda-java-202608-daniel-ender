# Secure-Endpoint Capstone — Student Starter

**Integrative capstone for the Spring Security & Microservices course.** One
endpoint, every control you have learned, proven by a single adversarial test
suite. This is a **red → green lab**: the suite ships with the project and most
of it is **failing on purpose**. Your job is to turn it all green by adding the
controls — without touching the tests.

## The build brief

You are hardening a `TransactionController` for the RTI Academy ledger service.
The starter compiles and runs, but it is wide open: anyone can post, malformed
input is accepted, and an internal failure leaks the database error straight to
the caller. Bring it up to production-grade security by combining **all** of the
course controls on this one endpoint:

| Control | Where it lives |
|---|---|
| **Bean Validation** | constraints on `TransactionRequest` + `@Valid` on the controller |
| **Authentication** (HTTP Basic, BCrypt) | `SecurityConfig` |
| **Role-based authorization** (least privilege) | `SecurityConfig` filter chain |
| **Secure transport** — security headers | `SecurityConfig` `.headers(...)` |
| **Secure transport** — least-privilege CORS | `SecurityConfig` `.cors(...)` + `CorsConfigurationSource` |
| **Deliberate CSRF stance** | `SecurityConfig` `.csrf(...)` (documented decision) |
| **Safe error handling** | a new `@ControllerAdvice` returning `ProblemDetail` |

The endpoints:

- `POST /api/transactions` — create a transaction. Body: `TransactionRequest`
  (`accountId` not blank, `amount` positive money with ≤2 decimals, `currency`
  a 3-letter uppercase code, `memo` ≤ 280 chars). Must require role
  `TRANSACTOR`.
- `GET /api/health` — public liveness probe. No auth.
- `GET /api/transactions/{id}` — fetch; id `boom` simulates an internal failure
  that must NOT leak its cause.

In-memory users (BCrypt): `viewer` / `viewer-pw` (role `VIEWER`),
`transactor` / `transactor-pw` (role `TRANSACTOR`).

## How to run

```bash
# Java 21 required.
mvn -q -B test     # run the adversarial suite (this is the lab)
mvn spring-boot:run   # run the app on :8080
```

## The adversarial suite and how red → green maps to the controls

`SecureEndpointAdversarialTest` (8 tests) is the spec. **Do not edit it.** On the
starter, only the two trivial tests pass; each remaining failure points you at
exactly one missing control:

| # | Test | Starter result | The control that turns it green |
|---|---|---|---|
| 1 | transactor + valid body → 201 | ✅ PASS | (happy path — already works) |
| 2 | anonymous POST → 401 | ❌ RED | **Authentication** — require auth; enable HTTP Basic |
| 3 | viewer POST → 403 | ❌ RED | **Authorization** — `POST /api/transactions` needs `hasRole("TRANSACTOR")` |
| 4 | invalid body → 400 `problem+json`, no leaked internals | ❌ RED | **Bean Validation** (`@Valid` + constraints) + **safe error handling** |
| 5 | `GET /transactions/boom` → 500 `problem+json`, generic, no internal detail | ❌ RED | **Safe error handling** — `@ControllerAdvice` returning a generic `ProblemDetail` |
| 6 | health anonymous → 200 | ✅ PASS | (public route — already works) |
| 7 | security headers (nosniff + `Referrer-Policy`) | ❌ RED | **Secure transport** — `.headers(...)` hardening |
| 8 | CORS preflight: `app.example.com` granted, `evil.example.com` denied | ❌ RED | **Secure transport** — least-privilege CORS |

Work through the `// TODO` markers in `TransactionRequest`, `TransactionController`,
and `SecurityConfig`, and create the `@ControllerAdvice`. When all 8 are green,
the endpoint is fully hardened.

> **CSRF note:** this is a stateless, per-request Basic-authenticated JSON API
> with no browser session to forge, so CSRF protection is *deliberately*
> disabled. The suite does not test CSRF directly — the point is that you make
> and document a stance rather than leave it to chance.

## Project layout

```
src/main/java/academy/rti/smc/
  Application.java
  TransactionController.java     # add @Valid; getById throws on "boom"
  TransactionRequest.java        # add constraints
  SecurityConfig.java            # chain + users + encoder + CORS (TODOs)
  (you create) GlobalExceptionHandler.java
src/test/java/academy/rti/smc/
  SecureEndpointAdversarialTest.java   # the spec — do not edit
```
