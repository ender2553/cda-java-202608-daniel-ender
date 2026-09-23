package com.cyberdev.pos.day3;

import com.cyberdev.pos.day1.PaymentMethod;

/**
 * Kept separate from Refundable (Day 1) -- authorizing a charge and refunding one are
 * different responsibilities with different implementers and different failure modes;
 * forcing them into one fat interface would violate interface segregation.
 */
public interface PaymentAuthorizer {
    AuthorizationResult authorize(PaymentMethod method, Money amount);
}
