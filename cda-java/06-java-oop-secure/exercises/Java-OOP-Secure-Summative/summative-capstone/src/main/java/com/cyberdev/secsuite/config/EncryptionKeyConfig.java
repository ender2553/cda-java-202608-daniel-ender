package com.cyberdev.secsuite.config;

import com.cyberdev.secsuite.exception.CryptoException;
import com.cyberdev.secsuite.security.EncryptionService;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Supplies the AES-256 key for EncryptionService (SEC-14) from the environment variable
 * SECSUITE_ENCRYPTION_KEY: the Base64 encoding of exactly 32 random bytes. Generate one with
 *
 *   openssl rand -base64 32
 *
 * Behaviour, deliberately asymmetric:
 *   - Variable NOT set   -> a fresh random key is generated for this run only, and a warning is
 *                           printed. Fine for the InMemory* demo (nothing outlives the process),
 *                           but anything encrypted under it is unreadable after exit -- so with
 *                           the Jdbc* repositories you MUST set the variable.
 *   - Variable set but malformed (not Base64, wrong length) -> CryptoException. FAIL CLOSED: an
 *                           operator who configured a key and typo'd it must find out now, not
 *                           discover weeks later that every row was encrypted under a throwaway
 *                           key nobody has.
 *
 * SECURITY CALLOUT: the key never appears in source code, in a properties file, or in any log
 * line -- the warning below says THAT a temporary key was generated, never WHAT it is.
 */
public final class EncryptionKeyConfig {

    public static final String ENV_VAR = "SECSUITE_ENCRYPTION_KEY";

    private EncryptionKeyConfig() {
    }

    public static SecretKey loadOrGenerateKey() {
        String configured = System.getenv(ENV_VAR);
        if (configured == null || configured.isBlank()) {
            System.out.println("[warn] " + ENV_VAR + " is not set -- using a temporary per-run AES-256 key. "
                    + "Encrypted values will NOT be readable after this run ends.");
            return EncryptionService.generateKey();
        }
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(configured.trim());
        } catch (IllegalArgumentException e) {
            throw new CryptoException(ENV_VAR + " is not valid Base64", e);
        }
        return EncryptionService.keyFromRawBytes(raw);
    }
}
