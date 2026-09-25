package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Locale;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Risk treatment status for a risk register entry. Values match the CHECK constraint on
 * risk_register_entry.status. These are the four classic risk-treatment outcomes (treat /
 * tolerate / transfer), with OPEN meaning "not yet treated".
 */
public enum RiskStatus {
    OPEN, MITIGATED, ACCEPTED, TRANSFERRED;

    /**
     * Strict, allow-list parse of an untrusted/persisted string (CSV field, database column).
     * Trims surrounding whitespace and upper-cases with Locale.ROOT (never the default locale:
     * in a Turkish locale "critical".toUpperCase() does not produce "CRITICAL"). Anything
     * outside the allowed set is rejected with a ValidationException -- fail closed, never a
     * silent default.
     */
    public static RiskStatus parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("RiskStatus must not be blank");
        }
        try {
            return RiskStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unrecognized RiskStatus value: '" + raw.trim() + "'");
        }
    }
}
