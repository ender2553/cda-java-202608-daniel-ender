# SMC Lab 04 — Authentication & Authorization (Instructor / Solution)

**Theme:** Spring Security Essentials — Authentication & Authorization.
**Control added this lesson:** authentication + **role-based authorization** with a
**deny-by-default**, **least-privilege** posture.

This is the GREEN half of a red/green lab. All tests in `TransactionAuthTest` PASS here.

## What's in scope

- **In-memory `UserDetailsService`** with two BCrypt-encoded users:
  - `viewer` / `viewer-pass` — role `VIEWER`
  - `transactor` / `transactor-pass` — role `TRANSACTOR`
- An **explicit** `SecurityFilterChain` (no reliance on Spring Security auto-config defaults):
  - `GET /api/health` → `permitAll()` (public)
  - `POST /api/transactions` → `hasRole("TRANSACTOR")`
  - everything else → `authenticated()`
  - HTTP Basic enabled (chosen for test simplicity).

## Carried-forward controls (not the focus)

- **Bean Validation** on `TransactionRequest` (`@NotBlank`, `@Positive`, …).
- **Secure transport**: explicit security response headers (CSP, frame-options deny)
  and a deliberately scoped CORS policy.

## Endpoints

| Method | Path                | Auth required        | Success |
|--------|---------------------|----------------------|---------|
| GET    | `/api/health`       | none (public)        | 200     |
| POST   | `/api/transactions` | role `TRANSACTOR`    | 201     |

## The contract (identical test in both trees)

| Scenario                                         | Expected |
|--------------------------------------------------|----------|
| Anonymous `POST /api/transactions`               | 401      |
| `viewer` `POST /api/transactions`                | 403      |
| `transactor` well-formed `POST /api/transactions`| 201      |
| Anonymous `GET /api/health`                       | 200      |

## Run

```bash
mvn -q -B test
```

Expected: **all green.**

## Stack

Java 21 · Spring Boot 3.3.5 · Maven · `academy.rti.smc` · artifactId `smc-04-authn-authz`.
