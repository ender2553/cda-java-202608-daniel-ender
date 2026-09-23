package com.cybersoft.vehicle.crypto;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/**
 * AES-GCM provides confidentiality + integrity. Key must come from a secret manager/environment, never source control.
 */
public final class FieldEncryptor {
    private static final int IV_BYTES = 12, TAG_BITS = 128;
    private final SecretKey key;
    private final SecureRandom rng = new SecureRandom();

    public FieldEncryptor(byte[] keyBytes) {
        if (keyBytes == null || (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32))
            throw new IllegalArgumentException("AES key must be 16/24/32 bytes");
        this.key = new SecretKeySpec(Arrays.copyOf(keyBytes, keyBytes.length), "AES");
    }

    public byte[] encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            throw new IllegalArgumentException("plaintext required");
        }

        try {
            byte[] iv = new byte[IV_BYTES];
            rng.nextBytes(iv);

            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, iv);
            c.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ct = c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return ByteBuffer.allocate(IV_BYTES + ct.length)
                    .put(iv)
                    .put(ct)
                    .array();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Encryption failed closed", e);
        }
    }

    public String decrypt(byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("invalid payload");
        }
        if (payload.length <= IV_BYTES) {
            throw new IllegalArgumentException("invalid payload");
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] ct = new byte[buffer.remaining()];
            buffer.get(ct);

            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, iv);
            c.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plaintext = c.doFinal(ct);
            return new String(plaintext, StandardCharsets.UTF_8);
        //} catch (AEADBadTagException e) {
            //throw new SecurityException("Decryption/authentication failed; access denied", e);
        } catch (GeneralSecurityException e) {
            throw new SecurityException("Decryption/authentication failed; access denied", e);
        }
    }
}
