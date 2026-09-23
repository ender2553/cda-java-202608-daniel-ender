package com.cyberdev.pos.day3;

import com.cyberdev.pos.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Set;

/**
 * An amount plus an allow-listed currency code. Records get a compact constructor
 * for free -- the natural place to enforce invariants that must hold for every
 * instance, no matter how it's built.
 *
 * NOTE: contrast with Day 1's Product (a manually-written immutable class) and
 * PaymentMethod (manual equals()/hashCode()) -- Money is the deliberate payoff of using a
 * record: equals()/hashCode()/toString() are generated for free and correct by construction,
 * because BigDecimal + String are themselves proper value types. No override is needed here
 * at all, which is worth calling out explicitly as the contrast case Day 1's INSTRUCTOR_GUIDE
 * already previews.
 */
public record Money(BigDecimal amount, String currencyCode) {

    private static final Set<String> ALLOWED_CURRENCIES = Set.of("USD", "CAD", "EUR", "GBP");

    // NOTE [POS3-1]: This is the compact constructor. It runs BEFORE the
    // implicit field assignment, so validation here rejects a bad Money before it can
    // ever exist -- there is no way to construct an invalid instance through this
    // canonical constructor. Concept tested: records + compact constructors as a cheap,
    // centralized place for invariant enforcement. Why it matters: every other
    // constructor students might add (or a copy constructor) still funnels through this
    // one field assignment, so the validation cannot be bypassed by construction path.
    // As of the custom-exception-hierarchy addition, this throws ValidationException
    // (com.cyberdev.pos.exception), the same type LineItem/PaymentMethod already throw --
    // reusing Day 1's exception type here, rather than a fresh Money-specific one, is
    // itself a small design point: Money's invalid-input failure mode is not meaningfully
    // different from LineItem's, so it does not need its own exception type.
    // Common mistake: validating in a static factory method instead of the compact
    // constructor, which leaves the canonical `new Money(...)` unprotected; or
    // normalizing currencyCode's case losslessly by uppercasing a LOCAL variable before
    // checking against the allow list, then forgetting to assign the normalized value
    // back to the compact constructor's implicit parameter -- a classic "validated the
    // local variable, not the field" bug. Fail-closed: unrecognized currency codes and
    // negative/null amounts are rejected, not defaulted to USD/0.
    // TODO [POS3-1]: Validate amount (non-null, non-negative) and currencyCode (non-null,
    // must be one of ALLOWED_CURRENCIES after uppercasing) -- throw ValidationException
    // (com.cyberdev.pos.exception) for each failure, fail closed. Remember to assign the
    // UPPERCASED value back to currencyCode (the compact constructor's implicit parameter)
    // before the implicit field assignment runs, not just validate a local variable.
    public Money {
        if (amount == null) {
            throw new ValidationException("amount must not be null");
        }

        if (amount.signum() < 0) {
            throw new ValidationException("amount must not be negative");
        }

        if (currencyCode == null) {
            throw new ValidationException("currencyCode must not be null");
        }

        currencyCode = currencyCode.toUpperCase();

        if (!ALLOWED_CURRENCIES.contains(currencyCode)) {
            throw new ValidationException("Unsupported currency code: " + currencyCode);
        }
    }

    public Money add(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new ValidationException("Cannot add Money of different currencies: "
                    + this.currencyCode + " vs " + other.currencyCode);
        }
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }
}
