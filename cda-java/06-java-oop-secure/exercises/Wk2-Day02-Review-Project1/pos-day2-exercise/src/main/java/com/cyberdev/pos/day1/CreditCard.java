package com.cyberdev.pos.day1;

import java.math.BigDecimal;

/**
 * A credit card payment method. Refundable back to the same card.
 */
public final class CreditCard extends PaymentMethod implements Refundable {

    private final CardBrand brand;
    private final int expirationMonth;
    private final int expirationYear;

    // INSTRUCTOR NOTE [POS1-6]: Two things graded here: (1) the explicit super(cardholderName,
    // last4) call so PaymentMethod's fail-closed validation actually runs for credit cards,
    // and (2) implementing both describe() (abstract from PaymentMethod) and refund()
    // (from Refundable). Common mistake: forgetting `super(...)` won't compile since
    // PaymentMethod has no no-arg constructor, so it's rare to skip; the more common
    // near-miss is a describe() that leaks the full card number/last4 in a careless format,
    // or a refund() that always returns true regardless of amount (fail-open on invalid
    // refund amounts). refund() should reject a null/negative/zero amount.
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

    @Override
    public String describe() {
        return brand + " card ending in " + getLast4() + " (" + getCardholderName() + ")";
    }

    @Override
    public boolean refund(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return false;
        }
        return true;
    }
}
