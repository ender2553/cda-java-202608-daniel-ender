package com.vitalledger.dto;

import com.vitalledger.domain.ClinicalRecord;

import java.time.Instant;

/** Response view of a clinical record. */
public record RecordResponse(
        Long id,
        Long patientId,
        String recordType,
        String note,
        String authoredBy,
        Instant createdAt
) {
    public static RecordResponse from(ClinicalRecord r) {
        return new RecordResponse(r.getId(), r.getPatientId(), r.getRecordType(),
                r.getNote(), r.getAuthoredBy(), r.getCreatedAt());
    }
}
