package com.cyberdev.dndrpg.security;

import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * DAY 4 ADDITION. Encrypts/decrypts PII-shaped data (e.g. a player's email
 * -- never a password, which is HASHED by PasswordHasher instead, never
 * encrypted) with AES-256-GCM. A fresh random 12-byte IV is generated for
 * EVERY encryption call; the IV is packaged together with the ciphertext,
 * then the whole thing is Base64-encoded into a single String, matching
 * Player.getEncryptedEmail()'s javadoc ("AES-256-GCM ciphertext,
 * Base64-encoded").
 *
 * Algorithm parameters (transformation, IV size, GCM tag length) match the
 * POS afternoon material's CardVault exactly.
 */
@Component
public final class EncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Convenience for generating an AES-256 key. NOTE: a real deployment
     * would pull this key from a secrets manager or KMS, never generate a
     * fresh one at process start with nowhere durable to keep it -- every
     * restart would make every previously-encrypted email undecryptable.
     * That's out of scope for this teaching codebase, which has no
     * key-management infrastructure at all; Main just generates one so the
     * demo has a key to use.
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES key generation unavailable", e);
        }
    }

    // NOTE: fresh random IV generated INSIDE this method, on
    // every single call -- never a field, never a constant, never derived
    // deterministically from the plaintext or key. GCM's confidentiality
    // and integrity guarantees both collapse if the same (key, IV) pair is
    // ever reused to encrypt two different messages. The IV is packaged
    // with the ciphertext (IV || ciphertext) and the combined bytes are
    // Base64-encoded so callers store/pass around a single String.
    public String encrypt(String plaintext, SecretKey key) {
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
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] packaged = ByteBuffer.allocate(IV_LENGTH_BYTES + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
            return Base64.getEncoder().encodeToString(packaged);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-GCM encryption failed", e);
        }
    }

    // NOTE: un-packages the IV that was prepended by encrypt()
    // for THIS SAME ciphertext (never a freshly generated one -- decryption
    // needs the exact IV used at encryption time). A tampered or truncated
    // ciphertext surfaces from the underlying Cipher as an
    // AEADBadTagException (or another GeneralSecurityException); rather
    // than letting that raw checked-exception type leak out (or, far
    // worse, silently returning garbage as if decryption "succeeded"),
    // this wraps it in an unchecked exception and re-throws.
    public String decrypt(String ciphertextBase64, SecretKey key) {
        if (ciphertextBase64 == null) {
            throw new IllegalArgumentException("ciphertextBase64 must not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        byte[] packaged;
        try {
            packaged = Base64.getDecoder().decode(ciphertextBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Stored value is not valid base64", e);
        }
        if (packaged.length < IV_LENGTH_BYTES) {
            throw new IllegalStateException("Stored value is too short to contain an IV");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(packaged);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            throw new IllegalStateException(
                    "Ciphertext failed integrity check (wrong key or tampered/truncated data)", e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-GCM decryption failed", e);
        }
    }
}
