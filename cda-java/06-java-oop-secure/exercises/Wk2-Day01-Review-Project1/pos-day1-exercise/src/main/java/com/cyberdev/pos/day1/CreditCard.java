package com.cyberdev.pos.day1;

import java.math.BigDecimal;

/**
 * A credit card payment method. Refundable back to the same card.
 *
 * TODO [POS1-6]: Make this class extend PaymentMethod and implement Refundable. The
 * constructor must call super(cardholderName, last4) explicitly, then validate/assign
 * brand, expirationMonth, expirationYear. Implement describe() (from PaymentMethod) and
 * refund() (from Refundable) -- refund() must fail closed (return false) for a
 * null/zero/negative amount.
 */
public final class CreditCard extends PaymentMethod implements Refundable {

    private final CardBrand brand;
    private final int expirationMonth;
    private final int expirationYear;

    public CreditCard(String cardholderName, String last4, CardBrand brand, int expirationMonth, int expirationYear) {
        super(cardholderName, last4);
        if (brand == null) {
            throw new IllegalArgumentException("brand must not be null");
        }
        if (expirationMonth < 1 || expirationMonth > 12) {
            throw new IllegalArgumentException("expirationMonth must be 1-12, was " + expirationMonth);
        }
        this.brand = brand;
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
    }

    public CardBrand getBrand() {
        return brand;
    }

    public int getExpirationMonth() {
        return expirationMonth;
    }

    public int getExpirationYear() {
        return expirationYear;
    }

    public boolean isExpired(int currentYear, int currentMonth) {
        if (expirationYear < currentYear) {
            return true;
        }
        return expirationYear == currentYear && expirationMonth < currentMonth;
    }

    // TODO [POS1-6]: return a human-readable description including the brand and last4,
    // e.g. "VISA card ending in 4242 (Alice)".
    @Override
    public String describe() {
        throw new UnsupportedOperationException("TODO [POS1-6]: implement describe() for CreditCard");
    }

    // TODO [POS1-6]: fail closed -- return false for a null, zero, or negative amount;
    // return true for a valid positive amount.
    @Override
    public boolean refund(BigDecimal amount) {
        throw new UnsupportedOperationException("TODO [POS1-6]: implement refund() for CreditCard, fail closed on bad amount");
    }
}
