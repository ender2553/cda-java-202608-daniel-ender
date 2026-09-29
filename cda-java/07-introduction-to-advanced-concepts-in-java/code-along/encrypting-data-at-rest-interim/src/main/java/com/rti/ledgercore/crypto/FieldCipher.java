package com.rti.ledgercore.crypto;

import com.rti.ledgercore.config.Env;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Field-level "protection" for LedgerCore's sensitive at-rest data (e.g. an
 * {@code AccountHolder}'s tax ID).
 *
 * <p><b>This is the insecure starting point for the lab.</b> It does not
 * actually encrypt anything — see the TODOs below.
 *
 * <p><b>Code-along order:</b> Step 1 is in {@code app.LedgerCoreApp}, Steps 2–9
 * are in this file (top to bottom), Steps 10–11 are back in
 * {@code app.LedgerCoreApp}.
 *
 * <p>What gets stored in {@code tax_id_encrypted} once you're done:
 * <pre>
 *   Base64( [ 12-byte IV ][ ciphertext ][ 16-byte GCM tag ] )
 * </pre>
 */
public final class FieldCipher {

    // COMPLETE (lab) Step 2 of 11 — Add the cipher settings as constants.
    //   - TRANSFORMATION = "AES/GCM/NoPadding"
    //       Always spell out all three parts. Plain "AES" silently means
    //       "AES/ECB/PKCS5Padding" — ECB is insecure (same input -> same output).
    //   - KEY_LENGTH_BYTES = 32   (AES-256)
    //   - IV_LENGTH_BYTES  = 12   (the standard GCM IV size)
    //   - TAG_LENGTH_BITS  = 128  (the GCM authentication tag)
    //   - one shared SecureRandom, used to generate every IV
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key; // TODO (lab): this key is currently unused — wire it into real AES-GCM encryption

    // COMPLETE (lab) Step 3 of 11 — Validate the key and turn it into a real AES key.
    //   - Throw IllegalArgumentException unless rawKeyBytes is exactly
    //     KEY_LENGTH_BYTES long (catches a missing or wrong-size key early).
    //   - Change the field above to a SecretKeySpec and store
    //     new SecretKeySpec(rawKeyBytes, "AES").
    public FieldCipher(byte[] rawKeyBytes) {
        if (rawKeyBytes == null || rawKeyBytes.length != KEY_LENGTH_BYTES){
            throw new IllegalArgumentException("AES-256 key must be exactly 32 bytes");
        }
        this.key = new SecretKeySpec(rawKeyBytes, "AES");
    }

    // INSECURE: the value is stored as-is — anyone with direct access to the
    // database or a backup can read it without going through the application.
    //
    // COMPLETE (lab) Step 4 of 11 — Generate a brand-new IV for THIS call.
    //   - byte[] iv = new byte[IV_LENGTH_BYTES]; then fill it from your SecureRandom.
    //   - Never reuse an IV with the same key — that breaks GCM completely.
    // COMPLETE (lab) Step 5 of 11 — Encrypt.
    //   - Cipher.getInstance(TRANSFORMATION)
    //   - init with Cipher.ENCRYPT_MODE, the key, and new GCMParameterSpec(TAG_LENGTH_BITS, iv)
    //   - doFinal(plaintext.getBytes(StandardCharsets.UTF_8)) — the result already
    //     has the 16-byte tag on the end.
    // COMPLETE (lab) Step 6 of 11 — Package the result for the database column.
    //   - Put the IV first, then the ciphertext, into one byte array
    //     (ByteBuffer.allocate(...).put(iv).put(ciphertext).array()).
    //   - Return it Base64-encoded, so it fits in the VARCHAR column.
    //   - The IV is not a secret — storing it next to the ciphertext is normal.
    public String encryptField(String plaintext) {
        //Step 4
        byte[] iv = new byte[IV_LENGTH_BYTES];
        RANDOM.nextBytes(iv);
        try {
            //Step 5
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            //Step 6
            byte[] stored = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
            return Base64.getEncoder().encodeToString(stored);
        } catch (GeneralSecurityException ex){
            throw new IllegalStateException("Could Not Encrypt Field", ex);
        }
    }

    // TODO (lab) Step 7 of 11 — Unpack the stored value (reverse of Step 6).
    //   - Base64-decode FIRST. (Most common bug: slicing the Base64 text instead
    //     of the decoded bytes.)
    //   - First IV_LENGTH_BYTES bytes = the IV; everything after = ciphertext + tag.
    //     Arrays.copyOfRange(...) does both slices.
    // TODO (lab) Step 8 of 11 — Decrypt.
    //   - Same as Step 5, but Cipher.DECRYPT_MODE with the IV you just sliced out.
    //   - doFinal(...), then new String(bytes, StandardCharsets.UTF_8).
    //   - If anyone changed a single byte, doFinal throws AEADBadTagException —
    //     GCM refuses to hand back tampered data.
    // TODO (lab) Step 9 of 11 — Fail safely in BOTH methods.
    //   - Catch GeneralSecurityException and throw
    //     new IllegalStateException("Could not decrypt field", ex)
    //     (or "Could not encrypt field"). Never put the plaintext, key, or
    //     stored value in the message.
    public String decryptField(String stored) {
        return stored;
    }

    /**
     * Reads the LedgerCore field-encryption key from the environment (or the
     * git-ignored {@code .env} file — see {@code config.Env}). Once the
     * lab wires up real AES-GCM encryption, this becomes the only place the
     * key is sourced from — never a literal in source.
     *
     * <p>Already written for you — Step 10 is where {@code LedgerCoreApp}
     * starts calling it.
     */
    public static FieldCipher fromEnvironment() {
        String base64Key = Env.require("LEDGERCORE_DATA_KEY");
        byte[] rawKeyBytes = Base64.getDecoder().decode(base64Key);
        return new FieldCipher(rawKeyBytes);
    }
}
