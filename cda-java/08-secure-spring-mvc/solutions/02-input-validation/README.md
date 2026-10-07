# Lesson 2 Lab — Controllers, Request Mapping, and Input Validation (SOLUTION)

**Secure Microservice Coding** · `academy.rti.smc` · `smc-02-input-validation`

This is the **instructor solution** for the Lesson 2 lab. It is the GREEN end
state of the red/green exercise: the control — **Jakarta Bean Validation** — is
present, so every test passes.

## The control this lab adds

**Jakarta Bean Validation** on a request DTO. Declaring the rules an input must
satisfy on the DTO, then enforcing them with `@Valid`, makes the controller
reject malformed input with **400 Bad Request** before any business logic runs.

## The app

```
POST /api/transactions
```

Binds a JSON body to `TransactionRequest`:

| Field       | Type         | Constraint                                                     |
|-------------|--------------|----------------------------------------------------------------|
| `accountId` | `String`     | `@NotBlank`                                                    |
| `amount`    | `BigDecimal` | `@NotNull @Positive @Digits(integer = 12, fraction = 2)`       |
| `currency`  | `String`     | `@Pattern(regexp = "^[A-Z]{3}$")`                             |
| `memo`      | `String`     | `@Size(max = 280)`                                            |

A valid request returns **201 Created**.

## What makes this GREEN

Compared with the student starter, two things are different:

1. **`TransactionRequest`** carries Jakarta constraints on every field
   (imported from `jakarta.validation.constraints.*`).
2. **`TransactionController`** annotates the body parameter with `@Valid`
   (`jakarta.validation.Valid`):

   ```java
   public ResponseEntity<Void> create(@Valid @RequestBody TransactionRequest request)
   ```

A violated constraint raises `MethodArgumentNotValidException`, which Spring
Boot's default error handling maps to **400 Bad Request** automatically — no
custom `@ExceptionHandler` needed.

## Red → Green

The test suite (`TransactionControllerTest`) is **identical** to the student
project and never changes. With validation in place:

- well-formed request → **201** ✅
- blank `accountId` → **400** ✅
- negative `amount` → **400** ✅
- `amount` with too many decimals → **400** ✅
- invalid `currency` (`usd`, not `USD`) → **400** ✅
- oversized `memo` (> 280 chars) → **400** ✅

## Build / test

```bash
mvn -q -B test     # all seven tests pass — BUILD SUCCESS
mvn -q -B package  # build the jar
```

Requires **Java 21** and **Maven**. Spring Boot **3.3.5**.
