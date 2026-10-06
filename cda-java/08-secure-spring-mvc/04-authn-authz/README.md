# SMC Lab 04 — Authentication & Authorization (Student / Starter)

**Theme:** Spring Security Essentials — Authentication & Authorization.
**Control to add this lesson:** authentication + **role-based authorization** with a
**deny-by-default**, **least-privilege** posture.

This is the RED half of a red/green lab. As shipped, the security control is **absent**:
the `SecurityFilterChain` calls `permitAll()` on everything, so anyone (anonymous or any
authenticated user) can POST a transaction. **The project compiles**, but the
auth/authz assertions in `TransactionAuthTest` **FAIL** until you add the control.

## Your task

Open `SecurityConfig.java` and follow the TODO:

> `// TODO: require authentication + hasRole('TRANSACTOR') on /api/transactions; keep /api/health public`

Replace the permissive `anyRequest().permitAll()` baseline with deny-by-default,
least-privilege rules:

- `GET /api/health` → public (`permitAll()`)
- `POST /api/transactions` → `hasRole("TRANSACTOR")`
- everything else → `authenticated()`

The in-memory users, BCrypt encoder, HTTP Basic, and carried-forward controls are
already wired for you.

## Users (already provided)

| Username     | Password          | Role         |
|--------------|-------------------|--------------|
| `viewer`     | `viewer-pass`     | `VIEWER`     |
| `transactor` | `transactor-pass` | `TRANSACTOR` |

## Carried-forward controls (not the focus, already present)

- **Bean Validation** on `TransactionRequest`.
- **Secure transport**: security response headers + scoped CORS.

## The contract (do not edit the test)

| Scenario                                         | Expected | Starter result (RED) |
|--------------------------------------------------|----------|----------------------|
| Anonymous `POST /api/transactions`               | 401      | got **201** ❌        |
| `viewer` `POST /api/transactions`                | 403      | got **201** ❌        |
| `transactor` well-formed `POST /api/transactions`| 201      | 201 ✅               |
| Anonymous `GET /api/health`                       | 200      | 200 ✅               |

## Run

```bash
mvn -q -B test
```

Expected before your fix: the two authentication/authorization assertions FAIL (RED).
After you implement the TODO: all green.

## Stack

Java 21 · Spring Boot 3.3.5 · Maven · `academy.rti.smc` · artifactId `smc-04-authn-authz`.
