# Lab 01 — Attack Surface Map (Instructor)

**Lesson 1 — HTTP, JSON, and the Spring MVC Request Lifecycle**

## What this lab is

This is the **instructor** copy of the baseline Spring Boot REST application.
The code here is **identical** to the student tree — same controller, same DTO,
same tests. This lab is **analytical**: learners map the attack surface rather
than toggle a security control, so there is no red/green difference between the
two trees.

The only difference is **`attack-surface.md`**: in the student tree it is an
empty TODO template; here it is the **completed reference solution**.

## The app

A Spring Boot app (`academy.rti.smc`) exposing:

| Method | Path                | Purpose                                            |
|--------|---------------------|----------------------------------------------------|
| `GET`  | `/api/health`       | Liveness check, returns `{"status":"ok"}`          |
| `POST` | `/api/transactions` | Accepts a `TransactionRequest` JSON body, returns 201 |

`TransactionRequest` fields: `accountId` (String), `amount` (BigDecimal),
`currency` (String), `memo` (String). There is intentionally **no validation
and no Spring Security** — that is the point: the surface is wide open so it can
be mapped.

## How to run

From this directory:

```bash
mvn test
```

`TransactionControllerTest` asserts:

- a well-formed `POST /api/transactions` returns **201 Created**
- `GET /api/health` returns **200** with `status: ok`

Both tests pass. They are *functional* tests — they demonstrate that the surface
works (and therefore exists), not that it is defended.

## Teaching the mapping task

Students complete the table in their own `attack-surface.md`. The completed
reference solution lives in **`attack-surface.md`** in this tree. Use it to
drive discussion: the goal is for students to trace untrusted input through the
full Spring MVC request lifecycle (dispatcher → handler mapping → argument
resolution / Jackson deserialization → handler method) and to recognize that the
attack surface is broader than just "the request body" — the path, headers,
content type, and each individual field are all entry points.
