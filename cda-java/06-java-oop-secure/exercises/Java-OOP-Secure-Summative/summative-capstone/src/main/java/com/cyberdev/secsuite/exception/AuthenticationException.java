package com.cyberdev.secsuite.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Thrown by AuthService.login (SEC-13) for EVERY login failure -- unknown username, wrong
 * password, blank input, or an unexpected internal failure -- always with the same generic
 * message, so the exception itself can never become a username-enumeration oracle. Also
 * thrown by AuthService.register when a username is already taken.
 */
public class AuthenticationException extends SecSuiteException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
