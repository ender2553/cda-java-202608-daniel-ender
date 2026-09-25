package com.cyberdev.secsuite.security;

import org.springframework.stereotype.Component;

import com.cyberdev.secsuite.exception.CryptoException;
import com.cyberdev.secsuite.exception.ValidationException;

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
import java.util.Base64;

/**
 * AES-256-GCM encryption of PII-shaped strings (the analyst contact email) -- SEC-14. Ported
 * from QuickPay's CardVault with two changes: the key is injected ONCE through the constructor
 * (manual DI -- callers never juggle a SecretKey per call), and the packaged IV || ciphertext is
 * Base64-encoded so it fits the analyst.encrypted_contact_email VARCHAR column.
 *
 * Where the key comes from is NOT this class's business -- see config.EncryptionKeyConfig.
 */
@Component
public final class EncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int AES_256_KEY_BYTES = 32;

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(SecretKey key) {
        if (key == null) {
            throw new ValidationException("key must not be null");
        }
        byte[] encoded = key.getEncoded();
        if (!"AES".equalsIgnoreCase(key.getAlgorithm()) || encoded == null || encoded.length != AES_256_KEY_BYTES) {
            throw new CryptoException("EncryptionService requires a 256-bit AES key");
        }
        this.key = key;
    }

    /** GIVEN helper: a fresh random AES-256 key. */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (GeneralSecurityException e) {
            throw new CryptoException("AES key generation unavailable", e);
        }
    }

    /** GIVEN helper: wraps 32 raw bytes as an AES key. */
    public static SecretKey keyFromRawBytes(byte[] rawKeyBytes) {
        if (rawKeyBytes == null || rawKeyBytes.length != AES_256_KEY_BYTES) {
            throw new CryptoException("An AES-256 key must be exactly 32 bytes");
        }
        return new SecretKeySpec(rawKeyBytes, "AES");
    }

    // GIVEN/PROVIDED IMPLEMENTATION -- students are not asked to implement cryptographic
    // primitives in this capstone. AES-GCM uses a FRESH random 12-byte IV
    // generated INSIDE this method on every call (never a field, never a constant, never
    // derived from the plaintext), packaged as IV || ciphertext+tag and Base64-encoded into one
    // storable string. Why it matters: GCM's confidentiality AND integrity both collapse if a
    // (key, IV) pair is ever reused -- an attacker holding two ciphertexts under the same IV
    // can recover the XOR of the plaintexts and forge tags. Any GeneralSecurityException is
    // wrapped in CryptoException. Common mistakes: (1) generating the IV once in the
    // constructor/field "for efficiency"; (2) `new Random()` or a zero-filled IV "for
    // reproducible tests"; (3) storing the IV in a separate column/field and then losing track
    // of which IV belongs to which row; (4) "AES/ECB/PKCS5Padding" or just "AES" (which
    // defaults to ECB on most providers) -- no IV, no integrity, identical plaintexts give
    // identical ciphertexts. Negative-control test: encrypt the same email twice and assert the
    // two outputs differ, while both decrypt back to the original.
    // SECURITY CALLOUT: encryption is only as strong as key management. This class never
    // logs, prints or persists the key; Main obtains it from SECSUITE_ENCRYPTION_KEY (see
    // EncryptionKeyConfig), never from source code.
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new ValidationException("plaintext must not be null");
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] packaged = ByteBuffer.allocate(IV_LENGTH_BYTES + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
            return Base64.getEncoder().encodeToString(packaged);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("AES-GCM encryption failed", e);
        }
    }

    // INSTRUCTOR NOTE [SEC-14]: GIVEN counterpart to encrypt(): un-packages the IV that
    // encrypt() prepended to THIS ciphertext. A tampered, truncated or wrong-key value fails
    // GCM's tag check with AEADBadTagException, which is surfaced as a CryptoException --
    // never swallowed into an empty string or garbage that a caller might trust.
    public String decrypt(String packagedBase64) {
        if (packagedBase64 == null) {
            throw new ValidationException("packagedBase64 must not be null");
        }
        byte[] stored;
        try {
            stored = Base64.getDecoder().decode(packagedBase64);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Stored value is not valid Base64", e);
        }
        if (stored.length < IV_LENGTH_BYTES + GCM_TAG_LENGTH_BITS / 8) {
            throw new CryptoException("Stored value is too short to contain an IV and authentication tag");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(stored);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            throw new CryptoException("Ciphertext failed integrity check (wrong key or tampered/truncated data)", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("AES-GCM decryption failed", e);
        }
    }
}
