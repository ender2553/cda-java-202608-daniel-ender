package com.cyberdev.secsuite.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Wraps checked java.security / javax.crypto failures (GeneralSecurityException,
 * AEADBadTagException, a malformed Base64 payload, a wrong-length key) from
 * EncryptionService (SEC-14), PasswordHasher (SEC-13) and EncryptionKeyConfig. A failed
 * integrity check must surface loudly as this exception -- never be swallowed and turned
 * into garbage/empty plaintext that a caller might trust.
 */
public class CryptoException extends SecSuiteException {

    private static final long serialVersionUID = 1L;

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
