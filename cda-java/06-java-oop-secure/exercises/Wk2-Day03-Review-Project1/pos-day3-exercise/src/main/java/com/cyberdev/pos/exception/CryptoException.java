package com.cyberdev.pos.exception;

/**
 * Day 3 (additive -- Day 1/Day 2's exception files are unmodified).
 *
 * Thrown by CardVault when the underlying javax.crypto/java.security machinery fails --
 * a bad/mismatched key, or a tampered/truncated ciphertext on decrypt (surfaced by AES-GCM
 * as an authentication-tag failure). CardVault wraps these in CryptoException rather than
 * letting a raw java.security.GeneralSecurityException (or one of its many checked
 * subtypes: NoSuchAlgorithmException, InvalidKeyException, AEADBadTagException, ...) leak
 * out, for the same reason DataAccessException wraps persistence failures in Day 2: callers
 * of this project's crypto utility should only ever need to know about POS-specific
 * exception types, not the zoo of checked exceptions the JCA API surfaces.
 */
public class CryptoException extends PosException {

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
