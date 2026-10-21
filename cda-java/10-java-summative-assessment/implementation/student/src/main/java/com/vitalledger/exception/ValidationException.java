package com.vitalledger.exception;

/**
 * Thrown by the validating service tier when input fails a fail-closed check.
 * The message is a <b>safe, caller-facing</b> summary — it never contains PHI,
 * SQL, stack detail, or internal class names.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String safeMessage) {
        super(safeMessage);
    }
}
