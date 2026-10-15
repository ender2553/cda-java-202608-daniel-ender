package com.vitalledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to create a clinical record. {@code recordType} is constrained to an
 * allow-list at the service tier; {@code note} is bounded in length here and
 * scanned for injection markers by the service before persistence.
 */
public record CreateRecordRequest(

        @NotBlank
        @Size(max = 40)
        String recordType,

        @NotBlank
        @Size(max = 8000, message = "note must be at most 8000 characters")
        String note
) {
}
