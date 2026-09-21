package com.cyberdev.pos.day1;

import com.cyberdev.pos.exception.ValidationException;

import java.util.Objects;

/**
 * Base class of the payment method inheritance hierarchy. Any concrete payment method
 * (CreditCard, GiftCard, ...) extends this and must go through its fail-closed validation.
 */
public abstract class PaymentMethod {

    private final String cardholderName;
    private final String last4;

    // TODO [POS1-5]: Validate cardholderName (must not be null/blank) and last4 (must be
    // exactly 4 digits -- use a regex or manual digit check). Throw ValidationException
    // (com.cyberdev.pos.exception), NOT a raw IllegalArgumentException, for either failure.
    // Only assign the fields once both checks pass. Every subclass constructor routes
    // through this one via super(...), so fixing validation here protects the whole
    // hierarchy.
    protected PaymentMethod(String cardholderName, String last4) {
        throw new UnsupportedOperationException("TODO [POS1-5]: validate cardholderName/last4 (throw ValidationException) and assign fields (fail closed)");
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public String getLast4() {
        return last4;
    }

    public abstract String describe();

    // TODO [POS1-10]: Override equals()/hashCode() for this abstract base class. Two
    // PaymentMethods are equal if they share the same cardholderName AND last4, REGARDLESS
    // of concrete subclass (a CreditCard and a GiftCard with the same holder/last4 should be
    // equal PaymentMethods) -- use `instanceof PaymentMethod`, NOT `getClass() ==
    // o.getClass()`, since all comparable state lives in this base class and no subclass
    // adds its own. Use Objects.hash(cardholderName, last4) for hashCode(). Remember:
    // overriding equals() WITHOUT hashCode() silently breaks HashSet/HashMap behavior.
}
