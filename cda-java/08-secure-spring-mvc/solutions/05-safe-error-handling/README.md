# SMC Lab 05 — Secure Error Handling (Solution)

**Theme:** error responses must never leak stack traces, exception class names, or
internal detail (DB connection strings, secrets). The added control for this lab is
**centralized exception handling** with `@RestControllerAdvice` + `ProblemDetail`
(RFC 7807), returning safe `application/problem+json` bodies while logging full detail
server-side.

**Pre-baked prior controls (carried forward, not the focus):** input validation
(`TransactionRequest`), secure transport / authn-authz wiring (`SecurityConfig`).
For this lab the API is `permitAll` so the tests isolate error-handling behavior.

## This is the SOLUTION (GREEN) tree

`GlobalExceptionHandler` (`@RestControllerAdvice`) is present and funnels every error
path into a safe `ProblemDetail`:

- `MethodArgumentNotValidException` → **400** `application/problem+json` with safe,
  field-level messages only.
- generic `Exception` → **500** `application/problem+json` with a **generic** message;
  the real exception (including the `jdbc:...password=secret...` string) is logged
  server-side via SLF4J and never placed in the response body.

Run the tests:

```bash
mvn -q -B test
```

Expected: **all GREEN**. Both error bodies are `application/problem+json`, contain no
stack trace, no exception class name, and no internal detail.

## Why it's safe

- `ProblemDetail.forStatusAndDetail(...)` produces RFC 7807 bodies served as
  `application/problem+json` automatically by Spring.
- The generic `Exception` handler returns a fixed, generic message; sensitive detail
  goes only to `log.error(...)`.
- `application.properties` defensively disables `server.error.include-*` leak channels.

## Layout

```
src/main/java/academy/rti/smc/
  Application.java
  TransactionController.java     POST /api/transactions, GET /api/transactions/{id}
  TransactionRequest.java        validated DTO
  SecurityConfig.java            permitAll for this lab
  GlobalExceptionHandler.java    THE ADDED CONTROL (@RestControllerAdvice + ProblemDetail)
src/main/resources/application.properties
src/test/java/academy/rti/smc/SafeErrorHandlingTest.java
```
