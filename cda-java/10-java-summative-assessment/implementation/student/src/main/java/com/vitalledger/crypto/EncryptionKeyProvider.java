package com.vitalledger.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves the AES key used for encryption at rest.
 *
 * <p>Security posture: the key material is supplied <b>exclusively</b> via
 * configuration bound from the environment (property {@code vitalledger.aes.key},
 * which Spring relaxed-binds from the {@code VITALLEDGER_AES_KEY} environment
 * variable). There is no default literal and no in-source fallback: if the key is
 * absent the application <b>fails fast at startup</b> with a message that names
 * only the missing configuration, never any key material.</p>
 */
@Component
public class EncryptionKeyProvider {

    private final SecretKey secretKey;

    public EncryptionKeyProvider(@Value("${vitalledger.aes.key:}") String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            // Fail-closed: no key => no boot. The message never contains key material.
            throw new IllegalStateException(
                    "Encryption key is not configured. Set the VITALLEDGER_AES_KEY environment "
                    + "variable (base64-encoded 16/24/32-byte AES key). See .env.example.");
        }
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(base64Key.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Configured encryption key is not valid base64.");
        }
        if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
            throw new IllegalStateException(
                    "Configured encryption key must decode to 16, 24, or 32 bytes (AES-128/192/256).");
        }
        this.secretKey = new SecretKeySpec(raw, "AES");
    }

    SecretKey secretKey() {
        return secretKey;
    }
}
