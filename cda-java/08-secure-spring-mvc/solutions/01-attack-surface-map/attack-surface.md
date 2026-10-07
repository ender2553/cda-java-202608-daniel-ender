# Attack Surface Map — Reference Solution

**Lesson 1 — HTTP, JSON, and the Spring MVC Request Lifecycle**

This is the completed reference for the baseline app. It maps every
untrusted-input entry point, the trust level of the data arriving there, and the
risk it introduces while the application is undefended (no validation, no Spring
Security).

> Reasoning frame: every value below originates outside the app and crosses the
> trust boundary during the Spring MVC request lifecycle — dispatcher → handler
> mapping → `HandlerMethodArgumentResolver` (here, Jackson deserialization of the
> `@RequestBody`) → the `createTransaction` / `health` handler methods.

## Entry points

| # | Entry point | Trust level | Risk |
|---|-------------|-------------|------|
| 1 | `POST /api/transactions` request body (the whole JSON document) | Untrusted | Deserialized by Jackson with no schema/size limit checks beyond defaults; a huge or deeply-nested body can drive memory/CPU pressure (DoS). No `@Valid`, so nothing rejects malformed-but-parseable input. |
| 2 | `TransactionRequest.accountId` (String) | Untrusted | Unbounded, unvalidated free text. Flows into the response ack and (in a real system) into account lookups — injection / IDOR / log-forging vector; no format, length, or ownership check. |
| 3 | `TransactionRequest.amount` (BigDecimal) | Untrusted | No range, sign, scale, or precision check. Negative, zero, or absurdly large/high-precision values pass straight through; business-logic abuse and precision-based DoS. |
| 4 | `TransactionRequest.currency` (String) | Untrusted | Not constrained to an ISO-4217 allow-list. Arbitrary strings accepted; downstream currency handling could be confused or used to bypass checks. |
| 5 | `TransactionRequest.memo` (String) | Untrusted | Unbounded free text echoed/stored without encoding. Stored-XSS / log-injection / oversized-payload vector. |
| 6 | Request path + handler routing (`/api/...`) | Untrusted | Path is attacker-controlled; with no security filter chain there is no authentication or authorization gate before a handler runs — every route is anonymously reachable. |
| 7 | HTTP headers (incl. `Content-Type`, `Accept`, `Content-Length`, custom) | Untrusted | `Content-Type` drives message-converter selection; spoofed or unexpected headers can steer parsing. No header validation, size capping, or auth header check. |
| 8 | HTTP method + content negotiation | Untrusted | Method and `Accept` are client-controlled and select the handler / response representation; relied on implicitly with no enforcement, so behavior is steered by untrusted metadata. |

## Notes / reasoning

- **The body is one entry point, but each field is its own.** A common student
  miss is to write a single row for "the JSON body." Each field is independently
  attacker-controlled and has a distinct downstream use, so each is its own
  surface (rows 2–5).
- **No `@Valid` / no constraints.** Because `TransactionRequest` carries no Bean
  Validation annotations and the handler does not annotate `@Valid`, Jackson
  populates the object and hands it straight to the handler. Deserialization
  succeeds for any well-formed JSON regardless of semantic correctness.
- **No Spring Security on the classpath.** There is no filter chain, so rows 6–8
  (path, headers, method/negotiation) are reachable anonymously. In a defended
  edition these would sit behind authentication/authorization plus input
  validation — which is the subject of later lessons. Here we only *map* them.
- **The tests confirm the surface exists.** The functional tests prove the
  endpoints accept untrusted input and respond, which is exactly what makes each
  row above a live entry point rather than a hypothetical one.
