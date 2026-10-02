package demo.payroll.crypto;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.security.MessageDigest;

@Component
public class PinHasher {

    private static final int ITERATIONS = 210_000;
    private final SecureRandom random = new SecureRandom();

    public String hash(String pin) {
        if (pin == null || !pin.matches("[0-9]{6}")) {
            throw new IllegalArgumentException("PIN must contain 6 digits");
        }

        byte[] salt = new byte[16];
        random.nextBytes(salt);

        byte[] hash = derive(pin, salt);

        return "pbkdf2:" + ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    private byte[] derive(String pin, byte[] salt) {
        char[] characters = pin.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(
                characters, salt, ITERATIONS, 256);

        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to process PIN");
        } finally {
            spec.clearPassword();
            Arrays.fill(characters, '\0');
        }
    }

    public boolean verify(String pin, String storedHash) {
        if (pin == null || !pin.matches("[0-9]{6}")) {
            return false;
        }

        if (storedHash == null) {
            throw new IllegalStateException("Invalid stored PIN format");
        }

        try {
            String[] parts = storedHash.split(":", -1);

            if (parts.length != 4
                    || !parts[0].equals("pbkdf2")
                    || !parts[1].equals(Integer.toString(ITERATIONS))) {
                throw new IllegalArgumentException();
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            if (salt.length != 16 || expectedHash.length != 32) {
                throw new IllegalArgumentException();
            }

            byte[] actualHash = derive(pin, salt);

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid stored PIN format");
        }
    }
}