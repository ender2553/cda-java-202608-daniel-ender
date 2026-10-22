package com.vitalledger.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.stereotype.Component;

/**
 * Authenticated symmetric encryption using AES in GCM mode.
 *
 * <p>Each {@link #encrypt(String)} call generates a fresh random 96-bit IV; the
 * output is {@code base64( IV || ciphertext||tag )}. GCM provides confidentiality
 * <i>and</i> integrity (a 128-bit authentication tag), so tampered ciphertext
 * fails to decrypt rather than returning garbage plaintext.</p>
 *
 * <p>This is the engine behind {@link EncryptedStringConverter}: PHI fields are
 * encrypted transparently on write and decrypted on read, so a raw database read
 * of those columns returns ciphertext only.</p>
 */
@Component
public class AesGcmCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;      // 96-bit IV recommended for GCM
    private static final int TAG_LENGTH_BITS = 128;     // full 128-bit auth tag
    private static final int TAG_LENGTH_BYTES = TAG_LENGTH_BITS / 8;

    private final EncryptionKeyProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmCipher(EncryptionKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String encrypt(String plaintext) {
        // TODO(phase B): AES-GCM encrypt with a fresh random 96-bit IV; return base64(IV || ciphertext||tag); return null for null input.

        // Return null when there is no plaintext to encrypt.
        if (plaintext == null) {
            return null;
        }

        // Generate a new random initialization vector for this encryption.
        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    keyProvider.secretKey(),
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv)
            );

            // GCM appends the authentication tag to the encrypted data.
            byte[] ciphertextAndTag =
                    cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Store the IV first, followed by the ciphertext and authentication tag.
            byte[] combined = new byte[iv.length + ciphertextAndTag.length];

            System.arraycopy(
                    iv, 0,
                    combined, 0,
                    iv.length
            );

            System.arraycopy(
                    ciphertextAndTag, 0,
                    combined, iv.length,
                    ciphertextAndTag.length
            );

            // Encode the combined bytes for database storage.
            return Base64.getEncoder().encodeToString(combined);

        } catch (GeneralSecurityException e) {
            // Do not expose plaintext, keys, or cryptographic details in the error.
            throw new IllegalStateException("Encryption failed.");
        }
    }

    public String decrypt(String encoded) {
        // TODO(phase B): AES-GCM decrypt — split IV from ciphertext, verify the auth tag, return UTF-8 plaintext; return null for null input.

        // Return null when there is no encrypted value.
        if (encoded == null) {
            return null;
        }

        final byte[] combined;

        try {
            // Decode the Base64 value retrieved from storage.
            combined = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid encrypted data.");
        }

        // Ensure the data contains an IV and at least a complete authentication tag.
        if (combined.length < IV_LENGTH_BYTES + TAG_LENGTH_BYTES) {
            throw new IllegalArgumentException("Invalid encrypted data.");
        }

        // Extract the first 12 bytes as the initialization vector.
        byte[] iv = Arrays.copyOfRange(
                combined, 0, IV_LENGTH_BYTES
        );

        // The remaining bytes contain the ciphertext and authentication tag.
        byte[] ciphertextAndTag = Arrays.copyOfRange(
                combined, IV_LENGTH_BYTES, combined.length
        );

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keyProvider.secretKey(),
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv)
            );

            // doFinal verifies the authentication tag before returning plaintext.
            byte[] plaintext = cipher.doFinal(ciphertextAndTag);

            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (GeneralSecurityException e) {
            // Fail closed if the data was tampered with or the key is incorrect.
            // Never return plaintext when authentication fails.
            throw new IllegalArgumentException("Unable to decrypt encrypted data.");
        }
    }
}

