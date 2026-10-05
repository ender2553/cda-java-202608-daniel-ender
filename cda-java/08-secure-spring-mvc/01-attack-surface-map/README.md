# Lab 01 — Attack Surface Map

**Lesson 1 — HTTP, JSON, and the Spring MVC Request Lifecycle**

## What this lab is

This is a baseline Spring Boot REST application. It is deliberately undefended:
no Spring Security, no input validation, no authentication. Your job is **not**
to fix it. Your job is to **trace and map its attack surface** — every place
where untrusted input crosses the boundary into the application.

This is an **analytical** lab. There is no "before/after" security toggle and no
red/green test difference. The tests here are *functional*: they prove the
endpoints work and therefore that the attack surface genuinely exists.

## The app

A Spring Boot app (`academy.rti.smc`) exposing:

| Method | Path                | Purpose                                            |
|--------|---------------------|----------------------------------------------------|
| `GET`  | `/api/health`       | Liveness check, returns `{"status":"ok"}`          |
| `POST` | `/api/transactions` | Accepts a `TransactionRequest` JSON body, returns 201 |

`TransactionRequest` fields: `accountId` (String), `amount` (BigDecimal),
`currency` (String), `memo` (String).

## How to run

From this directory:

```bash
mvn test
```

This runs `TransactionControllerTest`, which asserts:

- a well-formed `POST /api/transactions` returns **201 Created**
- `GET /api/health` returns **200** with `status: ok`

Both tests should pass.

## Your task — map the attack surface

Open **`attack-surface.md`**. It contains a TODO table with empty rows. For the
baseline app above, fill in **every untrusted-input entry point**, the **trust
level** of the data arriving there, and the **risk** it introduces while
undefended.

Think through the full Spring MVC request lifecycle:

- Where does the HTTP request first enter the app?
- How is the JSON body deserialized into `TransactionRequest`, and what is *not*
  checked during that step?
- Which individual fields carry untrusted data, and what could each one carry?
- What about the request path, headers, and content type?

Complete the table, then compare your reasoning with the instructor solution.
