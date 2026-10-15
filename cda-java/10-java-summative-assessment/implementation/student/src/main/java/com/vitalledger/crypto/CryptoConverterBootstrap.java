package com.vitalledger.crypto;

import org.springframework.stereotype.Component;

/**
 * Wires the Spring-managed {@link AesGcmCipher} into the JPA-managed
 * {@link EncryptedStringConverter} once, at context startup.
 */
@Component
public class CryptoConverterBootstrap {
    public CryptoConverterBootstrap(AesGcmCipher cipher) {
        EncryptedStringConverter.configure(cipher);
    }
}
