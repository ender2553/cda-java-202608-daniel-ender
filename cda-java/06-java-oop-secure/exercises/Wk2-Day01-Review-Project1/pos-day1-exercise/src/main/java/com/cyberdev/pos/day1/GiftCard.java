package com.cyberdev.pos.day1;

import java.math.BigDecimal;

/**
 * A store gift card payment method.
 *
 * TODO [POS1-7]: Make this class extend PaymentMethod (NOT implement Refundable -- a gift
 * card purchase is not refunded back to "the card" the way a credit card is; this is
 * deliberate, not an oversight). The constructor must call super(cardholderName, last4)
 * explicitly, then validate/assign balance (must be non-negative).
 */
public final class GiftCard extends PaymentMethod {

    private final BigDecimal balance;

    public GiftCard(String cardholderName, String last4, BigDecimal balance) {
        super(cardholderName, last4);
        if (balance == null || balance.signum() < 0) {
            throw new IllegalArgumentException("balance must be non-negative");
        }
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String describe() {
        return "Gift card ending in " + getLast4()
                + " (balance " + balance + ")";
    }
}

