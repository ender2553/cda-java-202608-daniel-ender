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

    // INSTRUCTOR NOTE [POS1-5]: last4 validation is the fail-closed checkpoint for the whole
    // hierarchy -- every subclass constructor must route through super(...), so fixing
    // validation here protects CreditCard, GiftCard, and any future subclass at once.
    // Common mistakes: regex that accepts non-digit characters (e.g. just checking
    // `length() == 4` without checking each character is a digit), or validating in the
    // subclass instead of here (defeats the point of centralizing it in the base class).
    // As of the custom-exception-hierarchy addition, both checks below must throw
    // ValidationException, not a raw IllegalArgumentException.
    protected PaymentMethod(String cardholderName, String last4) {
        if (cardholderName == null || cardholderName.isBlank()) {
            throw new ValidationException("cardholderName must not be blank");
        }
        if (last4 == null || !last4.matches("\\d{4}")) {
            throw new ValidationException("last4 must be exactly 4 digits, was '" + last4 + "'");
        }
        this.cardholderName = cardholderName;
        this.last4 = last4;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public String getLast4() {
        return last4;
    }

    public abstract String describe();

    // INSTRUCTOR NOTE [POS1-10]: equals()/hashCode() for an abstract base class in an
    // inheritance hierarchy. Two payment methods are considered equal here if they share the
    // same cardholderName and last4, REGARDLESS of concrete subclass -- a CreditCard and a
    // GiftCard with the same holder/last4 are treated as equal PaymentMethods. That is a
    // deliberate choice, not an oversight, and it is why this uses `instanceof PaymentMethod`
    // rather than `getClass() == o.getClass()`:
    //   - `getClass() == o.getClass()` would make equals() asymmetric-safe across subclasses
    //     (a CreditCard is never equal to a GiftCard) but ALSO breaks the moment a subclass
    //     adds its own equals() override that calls super.equals() and then narrows further --
    //     the classic Liskov-violation trap discussed in Effective Java Item 10.
    //   - `instanceof PaymentMethod` is the correct choice when, as here, all state that
    //     matters for equality lives in the base class and subclasses add no comparable state
    //     of their own; it keeps equals() symmetric and transitive across the whole hierarchy.
    // Common student mistake: overriding equals() but forgetting hashCode() -- this silently
    // breaks HashSet/HashMap (equal objects end up in different buckets), which is why the
    // graded test specifically checks both the equals() contract AND that a HashSet collapses
    // two equal-by-value instances to one entry.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentMethod)) return false;
        PaymentMethod other = (PaymentMethod) o;
        return cardholderName.equals(other.cardholderName) && last4.equals(other.last4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardholderName, last4);
    }
}
