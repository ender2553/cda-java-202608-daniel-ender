package com.vitalledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request to create a patient. Bean-validation annotations are the first,
 * allow-list-based fail-closed gate; the service tier re-validates business rules.
 */
public record CreatePatientRequest(

        @NotBlank
        @Pattern(regexp = "^MRN-[0-9]{4,10}$",
                 message = "medicalRecordNumber must match MRN-<digits>")
        String medicalRecordNumber,

        @NotBlank
        @Size(max = 120, message = "fullName must be at most 120 characters")
        String fullName,

        @NotBlank
        @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$",
                 message = "dateOfBirth must be an ISO date (YYYY-MM-DD)")
        String dateOfBirth
) {
}
