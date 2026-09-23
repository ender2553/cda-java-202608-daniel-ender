package com.cyberdev.pos.day3;

import com.cyberdev.pos.exception.CryptoException;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Encrypts/decrypts PII-shaped data (e.g. a customer email, a processor-issued token --
 * never a full PAN) with AES-256-GCM. A fresh random 12-byte IV is generated for EVERY
 * encryption call; the IV is packaged together with the ciphertext for storage (never
 * stored/transmitted separately, and never reused across calls).
 */
public final class CardVault {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (GeneralSecurityException e) {
            throw new CryptoException("AES key generation unavailable", e);
        }
    }

    public static SecretKeySpec keyFromRawBytes(byte[] rawKeyBytes) {
        return new SecretKeySpec(rawKeyBytes, "AES");
    }

    // NOTE [POS3-8]: Concept tested: AES-GCM with a FRESH random IV generated
    // INSIDE this method, on every single call -- never a field, never a constant, never
    // derived deterministically from the plaintext or key. Why it matters: GCM's
    // confidentiality and integrity guarantees both collapse if the same (key, IV) pair is
    // ever reused to encrypt two different messages -- an attacker who sees two
    // ciphertexts under the same IV can often recover the XOR of both plaintexts and forge
    // the authentication tag. The IV is packaged with the ciphertext (IV || ciphertext, one
    // byte[]) so callers store/pass around a single value instead of two, and decrypt()
    // below un-packages it -- this is the "IV+ciphertext travel together" pattern called
    // for by the task. Common mistakes: (1) hoisting the IV generation to a field/
    // constructor so every call reuses it -- encrypting the same key+IV twice is exactly
    // the bug this TODO exists to catch; (2) using `new Random()` or a fixed byte array
    // "for reproducibility in tests" instead of SecureRandom -- predictable IVs are nearly
    // as bad as reused ones. The negative-control check for this TODO: call encrypt twice
    // with the same plaintext+key and assert the two outputs (and therefore the two
    // embedded IVs) differ.
    // TODO [POS3-8]: Generate a FRESH byte[IV_LENGTH_BYTES] IV via `secureRandom` INSIDE
    // this method on every call (never a field/constant/deterministic value), encrypt with
    // AES/GCM/NoPadding using that IV, and return a single byte[] packaging IV || ciphertext
    // together (see decrypt() below, which expects this exact layout). Wrap any
    // GeneralSecurityException in CryptoException.
    public byte[] encrypt(String plaintext, SecretKey key) {
        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext must not be null");
        }

        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] ciphertext = cipher.doFinal(
                    plaintext.getBytes(StandardCharsets.UTF_8)
            );

            return ByteBuffer.allocate(IV_LENGTH_BYTES + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();

        } catch (GeneralSecurityException e) {
            throw new CryptoException("AES-GCM encryption failed", e);
        }
    }

    // NOTE [POS3-9]: Un-packages the IV that was prepended by encrypt() for
    // THIS SAME ciphertext (never a freshly generated one -- decryption needs the exact
    // IV used at encryption time). A tampered or truncated ciphertext surfaces from the
    // underlying Cipher as an AEADBadTagException (or another GeneralSecurityException);
    // rather than letting that raw checked-exception type leak out (or, far worse, letting
    // it fall through and return garbage bytes/an empty string as if decryption
    // "succeeded"), this wraps it in this project's own CryptoException and re-throws --
    // silently swallowing a failed integrity check is worse than crashing, since a caller
    // might otherwise store or process corrupted data as if it were genuine.
    // TODO [POS3-9]: Un-package `stored` (IV_LENGTH_BYTES of IV, then the ciphertext) --
    // see encrypt() above for the exact layout -- and decrypt with AES/GCM/NoPadding using
    // THAT IV (never a freshly generated one). Let a failed GCM authentication check (a
    // tampered/truncated ciphertext, or the wrong key) surface as this project's
    // CryptoException, never a raw AEADBadTagException/GeneralSecurityException and never a
    // silently-returned null/garbage string.
    public String decrypt(byte[] stored, SecretKey key) {
        if (stored == null) {
            throw new IllegalArgumentException("stored must not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (stored.length < IV_LENGTH_BYTES) {
            throw new CryptoException("Stored value is too short to contain an IV", null);
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(stored);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);

            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);  //TODO [POS3-9]: update with correct code
        } catch (AEADBadTagException e) {
            throw new CryptoException("Ciphertext failed integrity check (wrong key or tampered/truncated data)", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("AES-GCM decryption failed", e);
        }

    }

    /** A masked reference safe to print/log/store instead of a full PAN. */
    public static String maskedReference(String last4, String issuerToken) {
        return "**** **** **** " + last4 + " (token " + issuerToken + ")";
    }
}
