package com.cyberdev.pos.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO on its own, but the type that several existing
 * TODOs (POS1-1, POS1-5) must throw instead of a raw IllegalArgumentException.
 *
 * Thrown by constructor-time validation across the series (LineItem quantity, PaymentMethod
 * last4/cardholderName, Money's compact constructor) when a value fails a fail-closed check.
 */
public class ValidationException extends PosException {

    public ValidationException(String message) {
        super(message);
    }
}
