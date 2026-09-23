# QuickPay POS — Day 3: Authorization, Sealed Results & Cashier/PII Crypto

**Cyber Developer Program — Java Secure Coding Foundations**
**Graded Afternoon Exercise — Part 3 of 3**

## The story

Monday you built the cart and payment-method foundation. Tuesday you built real
PostgreSQL-backed persistence and fixed a SQL-injection bug. Today QuickPay POS needs to
actually **decide whether to accept a payment**, and needs a way for a **cashier to log in**
and for **PII (not the card number itself, but PII-shaped data like a customer email) to be
encrypted** rather than hashed, since — unlike a PIN — the business legitimately needs to
read it back later.

Your Day 1 code (`Cart`, `LineItem`, `Product`, `CardBrand`, `PaymentMethod`, `CreditCard`,
`GiftCard`, `TerminalRegister`, `Refundable`) and Day 2 code (`Merchant`,
`TransactionRecord`, `TransactionRepository`, `TransactionService`, the `Jdbc*`/`InMemory*`
repositories, `RowMapper`s) are included here **already fully working** — that's your
foundation, not something to redo. All of Day 1's and Day 2's tests still run against this
code and must still pass.

## What's already given

- Everything from Day 1 and Day 2 (see their own guides), unchanged.
- `com.cyberdev.pos.exception.AuthenticationException` / `CryptoException` — two more
  members of the `PosException` hierarchy, additive this day. `AuthenticationException` is
  thrown by `CashierAuthService.login`; `CryptoException` is thrown by `CardVault`.
- `AuthorizationResult` (sealed interface) — the permits list itself; you implement the
  three record leaves (`Approved`/`Declined`/`Error`) below.
- `PaymentAuthorizer` (interface) — you implement its one method in
  `SimulatedProcessorAuthorizer`.
- `CashierAccount` — the entity type holding a cashier id + PBKDF2 salt/hash. Read its
  INSTRUCTOR NOTE on why it is a plain class with identity-only `equals()`, not a record.
- `CashierAccountRepository` (interface) + `InMemoryCashierAccountRepository` +
  `JdbcCashierAccountRepository` + `CashierAccountRowMapper` — the full persistence trio for
  cashier accounts, **given/working**, mirroring Day 2's `Merchant` persistence pattern
  exactly. You are not asked to rebuild repository/RowMapper mechanics a third time; the
  grading focus this day is elsewhere.
- `schema/day3_schema.sql` — adds the `cashier_account` table, additive to Day 2's
  `schema/schema.sql` (which is unchanged). NOTE: you will need to configure your DatabaseConfig (reference day 2 settings)
- `Main.java` — a fully-wired console application assembling everything. It calls into several
  TODOs below, so it will not run end-to-end until you've implemented them — read it as the
  worked example of how the pieces fit together.

## What you need to build (TODOs)

| Tag | File | What to do |
|---|---|---|
| POS3-1 | `Money.java` | Compact constructor: validate amount/currencyCode, normalize currency case |
| POS3-2 | `Approved.java`, `Declined.java`, `Error.java` | Compact constructors: validate each record's own fields |
| POS3-3 | `SimulatedProcessorAuthorizer.java` | `authorize` — trust-boundary validation, fail closed to `Declined`/`Error` |
| POS3-4 / POS3-5 | `CheckoutService.java` | `checkout` — exhaustive sealed switch (no `default`), persist via `TransactionService` only on `Approved` |
| POS3-6 | `CashierAuthService.java` | `enroll` — PBKDF2 hash with a fresh per-account salt |
| POS3-7 | `CashierAuthService.java` | `login` — fail-closed, indistinguishable failure for unknown cashier vs. wrong PIN |
| POS3-8 | `CardVault.java` | `encrypt` — AES-256-GCM with a fresh IV every call |
| POS3-9 | `CardVault.java` | `decrypt` — round-trip, raise `CryptoException` on tampering |

POS3-10 and POS3-11 are tested as end-to-end compositions of the pieces above (login gate,
then full flow) — there is no separate TODO file for them; getting POS3-1 through POS3-9
right is what makes those two pass.

## Key concepts this exercise tests

- **Sealed interfaces + records as an exhaustive "outcome enum with data."**
  `AuthorizationResult` is the ONE sealed type in the whole series. A `switch` over it can
  omit `default` entirely and still be exhaustive, because the compiler knows the permits
  list is closed — and will refuse to compile if a case is missing.
- **`PosException` (thrown) vs. `AuthorizationResult` (returned as data).** A malformed
  `Money`/`CashierAccount` constructor argument throws `ValidationException`; a declined
  card is returned as `Declined`, never thrown. Ask yourself why, for each TODO, which
  mechanism applies.
- **Trust-boundary validation that fails closed.** `SimulatedProcessorAuthorizer` must
  never let a plausible-but-bad input (expired card, unrecognized brand, non-positive
  amount) escape as an exception — it must resolve to `Declined`/`Error`.
- **Constructor-injection discipline, continued from Day 2.** `CheckoutService` depends on
  `PaymentAuthorizer` and Day 2's real `TransactionService` — both via the constructor only.
  It must never build its own instance of either.
- **PBKDF2 password/PIN hashing with a per-account salt**, and a login check that does not
  leak which failure mode occurred (unknown user vs. wrong PIN).
- **AES-256-GCM encryption with a fresh IV every call**, and the crucial contrast: PINs are
  *hashed* (one-way), PII you need to read back later (like an email) is *encrypted*
  (two-way, recoverable with the key).

## How to run the tests

The test suite uses JUnit Jupiter (JUnit 5) through Maven.

```
Right click green Java folder under the test folder and select "Run All Tests" or Run each day individually.
```

Day 1's and Day 2's tests run again automatically (regression check) alongside Day 3's new
tests — if you accidentally broke something in a given file, you'll see it here too.


