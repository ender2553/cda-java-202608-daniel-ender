package com.cybersoft.vehicle.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.*;

/**
 * Passwords are hashed, not encrypted. PBKDF2 is built into the JDK; production systems may choose Argon2id/bcrypt via vetted libraries.
 */
public final class PasswordHasher {
    private static final SecureRandom RNG = new SecureRandom();
    private static final int ITERATIONS = 210_000, KEY_BITS = 256, SALT_BYTES = 16;

    public String hash(char[] password) {
        validate(password);
        byte[] salt = new byte[SALT_BYTES];
        RNG.nextBytes(salt);
        byte[] hash = derive(password, salt);
        return "pbkdf2_sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verify(char[] password, String encoded) {
        try {
            String[] p = encoded.split("\\$");
            if (p.length != 4 || !p[0].equals("pbkdf2_sha256")) return false;
            int it = Integer.parseInt(p[1]);
            if (it < 100_000 || it > 2_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(p[2]), expected = Base64.getDecoder().decode(p[3]);
            return MessageDigest.isEqual(expected, derive(password, salt, it));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static void validate(char[] p) {
        if (p == null || p.length < 12) throw new IllegalArgumentException("password must be at least 12 characters");
    }

    private static byte[] derive(char[] p, byte[] s) {
        return derive(p, s, ITERATIONS);
    }

    private static byte[] derive(char[] p, byte[] s, int i) {
        try {
            PBEKeySpec spec = new PBEKeySpec(p, s, i, KEY_BITS);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        }
    }
}
