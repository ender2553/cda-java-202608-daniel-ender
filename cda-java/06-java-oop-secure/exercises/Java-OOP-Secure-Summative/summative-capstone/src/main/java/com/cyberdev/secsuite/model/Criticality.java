package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Locale;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Business criticality of an asset. Values match the CHECK constraint on asset.criticality in
 * schema/schema.sql exactly. Drives the IMPACT side of a promoted risk (see
 * RiskRegisterService.promoteOpenFindings).
 */
public enum Criticality {
    LOW, MEDIUM, HIGH, CRITICAL;

    /**
     * Strict, allow-list parse of an untrusted/persisted string (CSV field, database column).
     * Trims surrounding whitespace and upper-cases with Locale.ROOT (never the default locale:
     * in a Turkish locale "critical".toUpperCase() does not produce "CRITICAL"). Anything
     * outside the allowed set is rejected with a ValidationException -- fail closed, never a
     * silent default.
     */
    public static Criticality parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Criticality must not be blank");
        }
        try {
            return Criticality.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unrecognized Criticality value: '" + raw.trim() + "'");
        }
    }
}
