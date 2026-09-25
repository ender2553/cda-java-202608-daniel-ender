package com.cyberdev.secsuite.security;

import org.springframework.stereotype.Component;

import com.cyberdev.secsuite.exception.CryptoException;
import com.cyberdev.secsuite.exception.ValidationException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PBKDF2WithHmacSHA256 password hashing for analyst accounts (SEC-13). Same algorithm and
 * parameters as QuickPay's CashierAuthService (120,000 iterations, 256-bit derived key, fresh
 * 16-byte SecureRandom salt per hash), packaged differently: the analyst table has ONE
 * password_hash column, so salt + parameters + hash travel together as one self-describing
 * string, the same idea as the IV travelling with the ciphertext in EncryptionService:
 *
 * <pre>
 *   pbkdf2_sha256$120000$&lt;Base64 salt&gt;$&lt;Base64 derived key&gt;
 * </pre>
 *
 * Storing the iteration count alongside the hash is what lets a real system raise the work
 * factor later: old hashes still verify with the count they were created with, and can be
 * re-hashed at the next successful login.
 *
 * Stateless apart from its SecureRandom, and deliberately separate from AuthService so the
 * hashing primitive can be tested (and reasoned about) on its own.
 */
@Component
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    /** Bounds on a STORED iteration count, so a tampered row cannot request 2^31 iterations (CPU DoS). */
    private static final int MIN_ITERATIONS = 100_000;
    private static final int MAX_ITERATIONS = 10_000_000;

    private final SecureRandom secureRandom = new SecureRandom();

    // GIVEN/PROVIDED IMPLEMENTATION -- students are not asked to implement cryptographic
    // primitives in this capstone. PBKDF2 uses a FRESH random salt per
    // hash() call (never a shared/static salt, never derived from the username), producing
    // one self-describing string. Why it matters: a unique salt defeats precomputed
    // rainbow tables and makes two analysts with the same password get different stored
    // values; the deliberate 120,000-iteration work factor makes offline brute force after a
    // database breach expensive. Common mistakes: (1) a static salt -- "works", but identical
    // passwords produce identical hashes; (2) MessageDigest SHA-256 of the password -- a fast
    // hash with no work factor; (3) `new Random()` instead of SecureRandom; (4) building the
    // stored string with a separator that can appear in Base64 output (':' and '$' are safe,
    // '+', '/' and '=' are NOT); (5) converting the char[] to a String "to make it easier" --
    // the whole point of char[] is that it can be wiped, a String lingers in the heap.
    // SECURITY CALLOUT: passwords are HASHED (one-way, only ever compared); contrast
    // SEC-14, where the contact email is ENCRYPTED (two-way) because the business
    // legitimately needs to read it back. Choosing the wrong primitive for either is a design
    // flaw no amount of careful coding fixes.
    public String hash(char[] password) {
        if (password == null || password.length == 0) {
            throw new ValidationException("password must not be empty");
        }
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        byte[] derived = pbkdf2(password, salt, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(derived);
    }

    // GIVEN/PROVIDED IMPLEMENTATION -- verification recomputes the hash with the
    // STORED salt and STORED iteration count (a fresh salt would never match) and compares in
    // constant time with MessageDigest.isEqual. Fail closed: a null/empty password, a
    // null/malformed stored string, a wrong prefix, a non-numeric or out-of-bounds iteration
    // count, or bad Base64 all return FALSE -- never throw a parsing exception up into the
    // login flow, and never return true. Common mistakes: Arrays.equals or String.equals on the
    // encoded hashes (works, but not constant-time -- flag as a near-miss); letting a
    // NumberFormatException/IllegalArgumentException escape from a corrupted row; trusting a
    // stored iteration count without bounds (a tampered row asking for Integer.MAX_VALUE
    // iterations turns every login attempt into a CPU denial of service).
    public boolean verify(char[] password, String stored) {
        if (password == null || password.length == 0 || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        int iterations;
        byte[] salt;
        byte[] expected;
        try {
            iterations = Integer.parseInt(parts[1]);
            salt = Base64.getDecoder().decode(parts[2]);
            expected = Base64.getDecoder().decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS
                || salt.length != SALT_LENGTH_BYTES || expected.length != KEY_LENGTH_BITS / 8) {
            return false;
        }
        byte[] attempted = pbkdf2(password, salt, iterations);
        return MessageDigest.isEqual(attempted, expected);
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new CryptoException("PBKDF2 algorithm unavailable", e);
        } finally {
            spec.clearPassword(); // PBEKeySpec keeps its own copy of the password; wipe it
        }
    }
}
