package com.cyberdev.pos.day1;

import java.math.BigDecimal;

/**
 * Capability interface for payment methods that support refunding money back to the
 * original instrument. Deliberately NOT implemented by every PaymentMethod subclass
 * (see GiftCard) -- this sets up the interface-segregation discussion continued on Day 3.
 */
public interface Refundable {
    boolean refund(BigDecimal amount);
}
