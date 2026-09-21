# QuickPay POS — Day 1: Cart & Payment Method Foundations

**Cyber Developer Program — Java Secure Coding Foundations/OOP**
**Graded Exercise — Part 1 of 4**

## The story

You're building **QuickPay POS**, a checkout system for a single-terminal retail register.
Today you'll build the foundation: a shopping cart, a catalog item, and the payment-method
class hierarchy the rest of the week's POS app will build on. This is a separate, graded
track from the Dungeon Crawler Arena code-along — it's a standalone credit-card
point-of-sale app you'll keep extending through day 4 until it's a complete,
runnable console app.

## What's already given

- `Product` — an immutable catalog item (id, name, unitPrice). Fully implemented, including
  `equals()`/`hashCode()` by all fields (a value-type choice — see the INSTRUCTOR NOTE in
  the source).
- `Refundable` — the refund capability interface. Fully implemented (it's one method).
- `com.cyberdev.pos.exception.PosException` / `ValidationException` — the base of a custom
  exception hierarchy used across all 4 days. Fully implemented; you don't build these, you
  **use** them (see below).
- The `com.cyberdev.pos.testkit` package — the test harness. Don't modify it.

## Custom exceptions, starting today

`PosException` (base) and `ValidationException` (extends it) are given infrastructure.
From today on, constructor-time validation across the series throws `ValidationException`,
not a raw `IllegalArgumentException` — POS1-1 and POS1-5 below both require this. This is
deliberately a **different** mechanism from the sealed `AuthorizationResult` you'll meet
Wednesday: a declined card is an *expected* business outcome (returned as data), while a
malformed quantity or last4 is a genuine *error condition* (thrown). Ask yourself as you go:
why does one get thrown and the other returned?

## What you need to build (TODOs)

Every stub method throws `UnsupportedOperationException("TODO [TAG]: ...")` telling you
exactly what to implement and which tag it corresponds to.

| Tag | File | What to do |
|---|---|---|
| POS1-1 | `LineItem.java` | Constructor validation: reject `quantity <= 0`, throw `ValidationException` |
| POS1-2 | `Cart.java` | `addItem` — validate and add to the cart |
| POS1-3 | `Cart.java` | `getItems` — return items **without leaking the live list** |
| POS1-4 | `CardBrand.java` | `fromNumber` — allow-list prefix matching, fail closed to `UNKNOWN` |
| POS1-5 | `PaymentMethod.java` | Constructor validation for `cardholderName`/`last4`, throw `ValidationException` |
| POS1-6 | `CreditCard.java` | Extend `PaymentMethod`, implement `Refundable`, `describe()`, `refund()` |
| POS1-7 | `GiftCard.java` | Extend `PaymentMethod` (no `Refundable`), `describe()` |
| POS1-8 | `TerminalRegister.java` | `recordTransactionId` — reject duplicates using the `Set` |
| POS1-9 | `TerminalRegister.java` | `enqueueForSettlement`/`settleNext` — FIFO `Queue` behavior |
| POS1-10 | `PaymentMethod.java` | `equals()`/`hashCode()` on the abstract base class, by `cardholderName`+`last4` |

## Key concepts this exercise tests

- **Encapsulation and aliasing**: does your `Cart` ever hand out a reference callers can
  use to mutate it from outside?
- **Fail-closed validation**: invalid input should be *rejected*, not silently accepted,
  coerced, or (for `CardBrand`) misclassified.
- **Inheritance**: `CreditCard` and `GiftCard` both extend `PaymentMethod`, but only
  `CreditCard` implements `Refundable`. This is deliberate — not every subclass needs
  every capability.
- **Collections**: `Set` for duplicate rejection, `Queue` for FIFO settlement batching,
  `Map` for running totals.
- **`equals()`/`hashCode()` across an inheritance hierarchy**: POS1-10 asks you to override
  both on an *abstract base class*. The classic trap is using `getClass() ==
  o.getClass()` instead of `instanceof` — think about what that does to equality between a
  `CreditCard` and a `GiftCard` that share the same holder/last4, and why `instanceof` is
  the right call here (all comparable state lives in the base class).
- **Custom exceptions vs. sealed results**: `ValidationException` (thrown) models an error;
  Wednesday's sealed `AuthorizationResult` (returned) models an expected outcome. Same app,
  two different tools for two different situations.

## How to run the tests

```
Right click on the green Java folder under test and slect 'Run All Tests'
```

This compiles everything under `src/main`, then `src/test`, and runs the full graded suite. 
Each test method is a real JUnit 5 `@Test`, still tagged with the
original `@GradedTest(tag=..., points=..., description=...)` rubric annotation. You'll
see a `PASS`/`FAIL` line per test, tagged with its TODO tag, and a summary with per-tag
point totals at the end.

Starter code **compiles cleanly** but most tests will **fail** until you implement the
TODOs above — that's expected. Work through the tags in order; some methods depend on
others being correct first (e.g. `CreditCard`/`GiftCard` both call `super(...)`, so
`PaymentMethod`'s constructor must be correct before either subclass can be constructed).


