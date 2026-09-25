package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Locale;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Lifecycle status of a scan finding. Values match the CHECK constraint on scan_finding.status.
 * Only OPEN findings count as active exposure: SEC-4's duplicate check and SEC-12's threat
 * intel correlation both deliberately ignore RESOLVED findings.
 */
public enum FindingStatus {
    OPEN, RESOLVED;

    /**
     * Strict, allow-list parse of an untrusted/persisted string (CSV field, database column).
     * Trims surrounding whitespace and upper-cases with Locale.ROOT (never the default locale:
     * in a Turkish locale "critical".toUpperCase() does not produce "CRITICAL"). Anything
     * outside the allowed set is rejected with a ValidationException -- fail closed, never a
     * silent default.
     */
    public static FindingStatus parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("FindingStatus must not be blank");
        }
        try {
            return FindingStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unrecognized FindingStatus value: '" + raw.trim() + "'");
        }
    }
}
