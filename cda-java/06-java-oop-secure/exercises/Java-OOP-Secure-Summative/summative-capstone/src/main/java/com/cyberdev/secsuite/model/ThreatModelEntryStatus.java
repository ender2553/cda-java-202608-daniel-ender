package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Locale;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Status of one identified threat inside a threat model. Values match the CHECK constraint on
 * threat_model_entry.status.
 */
public enum ThreatModelEntryStatus {
    IDENTIFIED, MITIGATED, ACCEPTED;

    /**
     * Strict, allow-list parse of an untrusted/persisted string (CSV field, database column).
     * Trims surrounding whitespace and upper-cases with Locale.ROOT (never the default locale:
     * in a Turkish locale "critical".toUpperCase() does not produce "CRITICAL"). Anything
     * outside the allowed set is rejected with a ValidationException -- fail closed, never a
     * silent default.
     */
    public static ThreatModelEntryStatus parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("ThreatModelEntryStatus must not be blank");
        }
        try {
            return ThreatModelEntryStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unrecognized ThreatModelEntryStatus value: '" + raw.trim() + "'");
        }
    }
}
