package demo.payroll.crypto;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/** Encrypts and decrypts stored account fields. */
@Component
public class BankCrypto {

    private final SecretKeySpec key;
    private final SecretKeySpec legacyKey;
    private final SecureRandom random = new SecureRandom();

    public BankCrypto(
            @Value("${PAYROLL_FIELD_KEY}") String encodedKey,
            @Value("${PAYROLL_LEGACY_KEY}") String oldKey) {

        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "PAYROLL_FIELD_KEY must decode to 32 bytes");
        }

        this.key = new SecretKeySpec(keyBytes, "AES");

        byte[] legacyBytes = oldKey.getBytes(StandardCharsets.UTF_8);

        if (legacyBytes.length != 16) {
            throw new IllegalArgumentException(
                    "PAYROLL_LEGACY_KEY must contain 16 UTF-8 bytes");
        }

        this.legacyKey = new SecretKeySpec(legacyBytes, "AES");
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    new GCMParameterSpec(128, iv));

            byte[] encrypted =
                    cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] packed = ByteBuffer
                    .allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            return "gcm:" + Base64.getEncoder().encodeToString(packed);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to encrypt field");
        }
    }

    public String decrypt(String encoded) {
        try {
            if (!encoded.startsWith("gcm:")) {
                throw new IllegalArgumentException("Unsupported field format");
            }

            byte[] packed =
                    Base64.getDecoder().decode(encoded.substring(4));

            if (packed.length < 28) {
                throw new IllegalArgumentException("Invalid encrypted field");
            }

            ByteBuffer buffer = ByteBuffer.wrap(packed);

            byte[] iv = new byte[12];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(128, iv));

            return new String(
                    cipher.doFinal(encrypted),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decrypt field");
        }
    }
    public String decryptLegacy (String encoded){
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, legacyKey);

            byte[] decrypted =
                    cipher.doFinal(Base64.getDecoder().decode(encoded));

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decrypt legacy field");
        }
    }
}
