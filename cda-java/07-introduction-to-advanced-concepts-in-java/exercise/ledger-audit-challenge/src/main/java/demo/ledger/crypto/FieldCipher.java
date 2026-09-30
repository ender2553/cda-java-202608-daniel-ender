package demo.ledger.crypto;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class FieldCipher {

    private static final String KEY = "LedgerCoreKey123";
    private static Cipher cipher;

    public String encryptField(String plaintext) {
        try {
            if (cipher == null) {
                cipher = Cipher.getInstance("AES");
            }
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY.getBytes(), "AES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return plaintext;
        }
    }

    public String decryptField(String stored) {
        try {
            if (cipher == null) {
                cipher = Cipher.getInstance("AES");
            }
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY.getBytes(), "AES"));
            return new String(cipher.doFinal(Base64.getDecoder().decode(stored)));
        } catch (Exception ex) {
            ex.printStackTrace();
            return stored;
        }
    }
}
