# Lesson 2 Lab — Controllers, Request Mapping, and Input Validation (STARTER)

**Secure Microservice Coding** · `academy.rti.smc` · `smc-02-input-validation`

## The control this lab adds

**Jakarta Bean Validation** on a request DTO.

A REST controller that blindly trusts whatever JSON a client sends is an
injection and data-integrity hazard. The fix is to declare the *rules* the
input must satisfy on the DTO itself, then tell Spring to enforce them. When a
rule is violated, Spring rejects the request with **400 Bad Request** before any
business logic runs.

## The app

A single endpoint:

```
POST /api/transactions
```

It binds a JSON body to `TransactionRequest`:

| Field       | Type         | Rule you will enforce                         |
|-------------|--------------|-----------------------------------------------|
| `accountId` | `String`     | not blank                                     |
| `amount`    | `BigDecimal` | required, positive, at most 2 decimal places  |
| `currency`  | `String`     | exactly 3 uppercase letters (e.g. `USD`)      |
| `memo`      | `String`     | at most 280 characters                        |

A valid request returns **201 Created**.

## Red → Green

This is a **red/green** lab. The test suite (`TransactionControllerTest`) is the
same here as in the instructor solution and never changes.

**Starter (this project) = control ABSENT.**
`TransactionRequest` has **no** validation annotations and the controller does
**not** use `@Valid`. Every request is accepted — even garbage — so the server
returns `201` for malformed input.

Run the tests:

```bash
mvn -q -B test
```

You will see **RED**: the project compiles and the well-formed test passes, but
the five bad-input tests **fail** because the controller returns `201` where the
test expects `400`:

- blank `accountId` → expected 400, got 201
- negative `amount` → expected 400, got 201
- `amount` with too many decimals → expected 400, got 201
- invalid `currency` → expected 400, got 201
- oversized `memo` → expected 400, got 201

## Your task — make it GREEN

1. Add Jakarta constraints to `TransactionRequest` (look for the
   `// TODO: add Bean Validation` markers):
   - `@NotBlank` on `accountId`
   - `@NotNull @Positive @Digits(integer = 12, fraction = 2)` on `amount`
   - `@Pattern(regexp = "^[A-Z]{3}$")` on `currency`
   - `@Size(max = 280)` on `memo`

   Import them from `jakarta.validation.constraints.*`.

2. Add `@Valid` to the controller parameter in `TransactionController`:

   ```java
   public ResponseEntity<Void> create(@Valid @RequestBody TransactionRequest request)
   ```

   Import `jakarta.validation.Valid`.

3. Re-run `mvn -q -B test`. All seven tests should now pass — **GREEN**.

A failed constraint raises `MethodArgumentNotValidException`, which Spring
Boot's default error handling maps to **400 Bad Request** automatically. No
custom exception handler is required for this lab.

## Build / test

```bash
mvn -q -B test     # run the suite
mvn -q -B package  # build the jar
```

Requires **Java 21** and **Maven**. Spring Boot **3.3.5**.
