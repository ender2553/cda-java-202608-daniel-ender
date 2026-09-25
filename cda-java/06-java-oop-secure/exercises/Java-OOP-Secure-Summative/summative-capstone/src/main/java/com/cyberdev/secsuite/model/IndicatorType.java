package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Locale;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * The kind of indicator a threat intelligence alert carries. Values match the CHECK constraint
 * on threat_intel_alert.indicator_type. Only CVE indicators are correlated against scan
 * findings (SEC-12); an IP/DOMAIN/FILE_HASH alert that happens to carry a related CVE id is
 * context, NOT evidence that a vulnerable asset is being targeted, so it must never produce a
 * correlation hit.
 */
public enum IndicatorType {
    IP, DOMAIN, FILE_HASH, CVE;

    /**
     * Strict, allow-list parse of an untrusted/persisted string (CSV field, database column).
     * Trims surrounding whitespace and upper-cases with Locale.ROOT (never the default locale:
     * in a Turkish locale "critical".toUpperCase() does not produce "CRITICAL"). Anything
     * outside the allowed set is rejected with a ValidationException -- fail closed, never a
     * silent default.
     */
    public static IndicatorType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("IndicatorType must not be blank");
        }
        try {
            return IndicatorType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unrecognized IndicatorType value: '" + raw.trim() + "'");
        }
    }
}
