package org.example.util;

/**
 * Thrown by InputValidator (and the *Parser classes that use it) when
 * untrusted input fails a validation check at the trust boundary.
 *
 * This is a CHECKED exception on purpose: it forces every caller that
 * touches untrusted data to consciously decide how to handle a rejection
 * (skip the record? abort the whole batch? surface a generic error?)
 * rather than letting a validation failure disappear into an unchecked
 * exception that might get swallowed by an overly broad catch block.
 *
 * PROVIDED FOR YOU -- do not modify.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
