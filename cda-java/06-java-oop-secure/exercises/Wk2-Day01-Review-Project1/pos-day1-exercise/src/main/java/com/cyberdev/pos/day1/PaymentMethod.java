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

    // TODO [POS1-5]: Validate cardholderName and last4.
    protected PaymentMethod(String cardholderName, String last4) {

        if (cardholderName == null || cardholderName.isBlank()) {
            throw new ValidationException("Cardholder name must not be null or blank");
        }

        if (last4 == null || !last4.matches("\\d{4}")) {
            throw new ValidationException("Last4 must be exactly 4 digits");
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

    // TODO [POS1-10]: Override equals()/hashCode().
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PaymentMethod)) {
            return false;
        }

        PaymentMethod that = (PaymentMethod) o;

        return Objects.equals(cardholderName, that.cardholderName)
                && Objects.equals(last4, that.last4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardholderName, last4);
    }
}

