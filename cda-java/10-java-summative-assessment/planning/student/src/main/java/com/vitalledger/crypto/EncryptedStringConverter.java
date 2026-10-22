package com.vitalledger.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.springframework.stereotype.Component;

/**
 * JPA attribute converter that transparently encrypts a String column at rest.
 *
 * <p>Apply with {@code @Convert(converter = EncryptedStringConverter.class)} on any
 * PHI field. On write the value is AES-GCM encrypted; on read it is decrypted. A raw
 * SQL {@code SELECT} against the column therefore returns ciphertext.</p>
 *
 * <p>JPA converters are instantiated by the persistence provider, not by Spring, so
 * the cipher is injected through a static hook set once at context startup by
 * {@link CryptoConverterBootstrap}. All database access happens after context
 * initialization, so the cipher is always present when a conversion actually runs.</p>
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static volatile AesGcmCipher cipher;

    static void configure(AesGcmCipher c) {
        cipher = c;
    }

    private static AesGcmCipher cipher() {
        AesGcmCipher c = cipher;
        if (c == null) {
            throw new IllegalStateException("Encryption is not initialized.");
        }
        return c;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // TODO(phase B): encrypt the attribute via cipher() so the column stores ciphertext at rest.

        // Preserve null values instead of attempting to encrypt them.
        if (attribute == null) {
            return null;
        }

        // Encrypt the value before it is written to the database.
        return cipher().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // TODO(phase B): decrypt the stored ciphertext via cipher() when the entity is read back.

        // Preserve null values instead of attempting to decrypt them.
        if (dbData == null) {
            return null;
        }

        // Decrypt the database value before returning it to the entity.
        return cipher().decrypt(dbData);
    }
}

