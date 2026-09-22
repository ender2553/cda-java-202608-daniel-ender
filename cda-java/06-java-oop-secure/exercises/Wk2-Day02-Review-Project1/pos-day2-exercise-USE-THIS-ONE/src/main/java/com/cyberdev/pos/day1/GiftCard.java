package com.cyberdev.pos.day1;

import java.math.BigDecimal;

/**
 * A store gift card payment method. Extends PaymentMethod like CreditCard, but does NOT
 * implement Refundable -- a gift card purchase is not refunded back to "the card" the way
 * a credit card is. This demonstrates that a subclass in an inheritance hierarchy does not
 * have to pick up every capability interface its siblings do.
 */
public final class GiftCard extends PaymentMethod {

    private final BigDecimal balance;

    // INSTRUCTOR NOTE [POS1-7]: Graded points here are (1) extends PaymentMethod with a
    // correct super(...) call and (2) deliberately does NOT implement Refundable. A common
    // near-miss: a student "completes" GiftCard by also implementing Refundable just to be
    // safe/consistent with CreditCard -- that passes a naive "does it compile" check but
    // misses the interface-segregation lesson (some payment methods legitimately do not
    // support every capability) and should be called out in manual review, not just an
    // automated pass/fail.
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
        return "Gift card ending in " + getLast4() + " (balance " + balance + ")";
    }
}
