package com.cyberdev.dndrpg.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * DAY 4 ADDITION. Hashes and verifies player passwords using
 * PBKDF2WithHmacSHA256 with a fresh random salt per call. Never stores or
 * logs the raw password -- see Player's javadoc ("no plaintext password,
 * anywhere, ever").
 *
 * The salt and the derived hash are packaged into ONE stored string
 * (base64(salt) + ":" + base64(hash)) so callers -- AuthService, and
 * eventually PlayerRepository -- only ever need to persist a single
 * column, exactly what Player.getPasswordHash() already assumes.
 *
 * Parameters (iteration count, salt size, key length) match the POS
 * afternoon material's CashierAuthService exactly -- same algorithm, same
 * work factor, renamed to this domain's vocabulary.
 */
@Component
public final class PasswordHasher {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Hashes a raw password with a fresh random salt and returns a single
     * string encoding both the salt and the resulting hash, ready to store
     * as Player.passwordHash.
     */
    public String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password must not be empty");
        }
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Recomputes the hash using the SALT EMBEDDED IN storedHash (never a
     * fresh one -- a fresh salt would never match) and compares it against
     * the stored hash using MessageDigest.isEqual, the security-conscious
     * idiom for comparing secrets (constant-time-ish, avoids the short-
     * circuiting behavior of == or Arrays.equals on secret bytes).
     */
    public boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] attemptedHash = pbkdf2(password, salt);
            return MessageDigest.isEqual(attemptedHash, expectedHash);
        } catch (IllegalArgumentException e) {
            // Malformed base64 in storedHash -- treat as "does not verify",
            // never let a decoding failure surface as some other exception.
            return false;
        }
    }

    private byte[] pbkdf2(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 algorithm unavailable", e);
        }
    }
}
