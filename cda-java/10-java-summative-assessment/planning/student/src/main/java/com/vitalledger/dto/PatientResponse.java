package com.vitalledger.dto;

import com.vitalledger.domain.Patient;

/** Response view of a patient (decrypted PHI, returned only to authorized roles). */
public record PatientResponse(
        Long id,
        String medicalRecordNumber,
        String fullName,
        String dateOfBirth
) {
    public static PatientResponse from(Patient p) {
        return new PatientResponse(p.getId(), p.getMedicalRecordNumber(),
                p.getFullName(), p.getDateOfBirth());
    }
}
