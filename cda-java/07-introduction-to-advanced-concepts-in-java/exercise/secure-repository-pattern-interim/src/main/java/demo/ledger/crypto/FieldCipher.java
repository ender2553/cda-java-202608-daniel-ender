package demo.ledger.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-GCM field encryption, from Lesson 2 (Data at Rest). Not re-taught here:
 * this lesson changes only <i>where</i> it is called from — the repository.
 *
 * <p>Stored format: {@code Base64( [12-byte IV][ciphertext][16-byte GCM tag] )}.
 */
@Component
public class FieldCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    /** Spring injects the key from .env or the environment — never a literal in source. */
    public FieldCipher(@Value("${LEDGERCORE_DATA_KEY}") String base64Key) {
        byte[] rawKey = Base64.getDecoder().decode(base64Key.trim());
        if (rawKey.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("LEDGERCORE_DATA_KEY must be a Base64-encoded 32-byte key");
        }
        this.key = new SecretKeySpec(rawKey, "AES");
    }

    public String encryptField(String plaintext) {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        RANDOM.nextBytes(iv);   // a fresh IV for every call
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] stored = ByteBuffer.allocate(iv.length + ciphertext.length).put(iv).put(ciphertext).array();
            return Base64.getEncoder().encodeToString(stored);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not encrypt field", ex);
        }
    }

    public String decryptField(String stored) {
        try {
            byte[] decoded = Base64.getDecoder().decode(stored);
            byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(decoded, IV_LENGTH_BYTES, decoded.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            // Wrong key, tampered value, or a value that was never encrypted (not Base64).
            throw new IllegalStateException("Could not decrypt field", ex);
        }
    }
}
