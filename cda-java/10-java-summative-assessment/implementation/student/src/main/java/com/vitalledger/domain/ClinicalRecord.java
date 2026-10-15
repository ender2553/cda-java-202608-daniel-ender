package com.vitalledger.domain;

import com.vitalledger.crypto.EncryptedStringConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A clinical note attached to a patient. The clinical {@code note} (free-text PHI)
 * is encrypted at rest. {@code patientId} is stored as a plain foreign-key scalar;
 * the record does not hold a mutable reference to the {@link Patient} aggregate.
 */
@Entity
@Table(name = "clinical_records")
public class ClinicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /** Non-PHI category label (allow-listed at the service tier). */
    @Column(name = "record_type", nullable = false, length = 40)
    private String recordType;

    /** PHI — encrypted at rest. */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "note", nullable = false, length = 8000)
    private String note;

    @Column(name = "authored_by", nullable = false, length = 64)
    private String authoredBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ClinicalRecord() {
        // JPA
    }

    public ClinicalRecord(Long patientId, String recordType, String note, String authoredBy) {
        this.patientId = patientId;
        this.recordType = recordType;
        this.note = note;
        this.authoredBy = authoredBy;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getRecordType() {
        return recordType;
    }

    public String getNote() {
        return note;
    }

    public String getAuthoredBy() {
        return authoredBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
