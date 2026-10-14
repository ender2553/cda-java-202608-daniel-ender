package com.vitalledger.domain;

import com.vitalledger.crypto.EncryptedStringConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A patient. The direct identifiers ({@code fullName}, {@code dateOfBirth}) are PHI
 * and are encrypted at rest via {@link EncryptedStringConverter}. The externally
 * visible {@code medicalRecordNumber} is a non-PHI surrogate key used for lookups.
 *
 * <p>Reference security: fields are private with no setters; the entity is immutable
 * after construction from the caller's perspective (only JPA writes the generated id).</p>
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Non-PHI business identifier (e.g., "MRN-100427"). Safe to log / index. */
    @Column(name = "medical_record_number", nullable = false, unique = true, length = 32)
    private String medicalRecordNumber;

    /** PHI — encrypted at rest. */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** PHI — encrypted at rest. Stored as ISO-8601 text so the column is ciphertext. */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "date_of_birth", nullable = false)
    private String dateOfBirth;

    protected Patient() {
        // JPA
    }

    public Patient(String medicalRecordNumber, String fullName, String dateOfBirth) {
        this.medicalRecordNumber = medicalRecordNumber;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
    }

    public Long getId() {
        return id;
    }

    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }
}
