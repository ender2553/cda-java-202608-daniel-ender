package com.cyberdev.secsuite.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Thrown whenever input fails a fail-closed validation check: a model constructor refusing
 * to build an invalid object (SEC-1), an out-of-range CVSS score (SEC-2), an out-of-range
 * likelihood/impact (SEC-5), or a service refusing to act on an id that does not exist
 * (SEC-4, SEC-7, SEC-8). "Fail closed" means: reject and stop, never silently substitute a
 * default value and carry on.
 */
public class ValidationException extends SecSuiteException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
