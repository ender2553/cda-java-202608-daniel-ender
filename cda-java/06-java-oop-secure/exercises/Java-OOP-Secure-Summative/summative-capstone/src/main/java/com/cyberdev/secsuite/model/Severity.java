package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Locale;

/**
 * CVSS v3.x qualitative severity rating, plus the classifier that derives it from a numeric
 * base score (SEC-2).
 *
 * DESIGN DECISION -- NONE is a real value here, but it is NEVER STORED:
 *   - CVSS v3 defines five ratings: None (0.0), Low, Medium, High, Critical. A classifier that
 *     pretends 0.0 is "LOW" is lying, and one that throws on 0.0 rejects a legitimate score.
 *     So fromCvssScore(0.0) returns NONE.
 *   - cve_catalog does NOT store a severity column at all -- severity is a pure function of
 *     cvss_score, so storing both would be a transitive dependency (cve_id -> cvss_score ->
 *     severity), i.e. a 3NF violation with a built-in update anomaly (re-score a CVE, forget to
 *     update its severity). Severity for a CVE is always DERIVED at read time via this method.
 *   - The only stored severity is threat_intel_alert.severity, which is the FEED's own
 *     judgement (not derived from anything we hold), and its CHECK constraint allows only
 *     LOW/MEDIUM/HIGH/CRITICAL: an intel feed telling us about something with severity "none"
 *     is not an alert. {@link #parseStoredAlertSeverity(String)} enforces the same rule at the
 *     application layer so a CSV row with severity NONE is rejected before it ever reaches the
 *     database.
 */
public enum Severity {
    NONE, LOW, MEDIUM, HIGH, CRITICAL;

    // INSTRUCTOR NOTE [SEC-2]: Concept tested: turning a continuous, untrusted numeric input
    // into a closed set of categories with NO gaps and NO overlaps, and failing closed on
    // anything outside the domain. CVSS v3 bands: 0.0 -> NONE, 0.1-3.9 -> LOW,
    // 4.0-6.9 -> MEDIUM, 7.0-8.9 -> HIGH, 9.0-10.0 -> CRITICAL; < 0.0, > 10.0 or NaN ->
    // ValidationException. The robust way to write this is with strict "less than the NEXT
    // band's lower bound" comparisons (score < 4.0, score < 7.0, score < 9.0) rather than the
    // literal "<= 3.9 / <= 6.9 / <= 8.9" upper bounds printed in the spec: CVSS scores are
    // published to one decimal place, but a double like 3.95 (or an arithmetic result like
    // 6.9000000001) would fall into NO band under the "<= 3.9" style and silently hit whatever
    // the fall-through case is. Common mistakes: (1) an off-by-one at a boundary -- 4.0 must be
    // MEDIUM, 7.0 HIGH, 9.0 CRITICAL, and the seed data deliberately contains CVEs at exactly
    // 0.0, 3.9, 4.0, 6.9, 7.0, 8.9 and 9.0 to catch this; (2) treating 0.0 as LOW; (3) a
    // trailing `else return CRITICAL;` that silently classifies 11.0 or -1.0 as CRITICAL/LOW
    // instead of rejecting it; (4) forgetting NaN -- every comparison with NaN is false, so a
    // naive range check lets NaN through to the fall-through branch.
    // SECURITY CALLOUT: this classification drives what the report tells a human to fix first.
    // A classifier that fails OPEN (silently puts a malformed score in some bucket) produces a
    // report that looks authoritative and is wrong -- worse than no report at all.
    public static Severity fromCvssScore(double score) {
        throw new UnsupportedOperationException(
                "TODO [SEC-2]: classify a CVSS v3 base score into NONE/LOW/MEDIUM/HIGH/CRITICAL; reject < 0.0, > 10.0 and NaN");
    }

    /**
     * GIVEN -- not part of SEC-2. Strict parse of a STORED alert severity (CSV field or
     * threat_intel_alert.severity column): must be one of LOW/MEDIUM/HIGH/CRITICAL. NONE is
     * rejected on purpose -- see the class-level design note.
     */
    public static Severity parseStoredAlertSeverity(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("severity must not be blank");
        }
        Severity parsed;
        try {
            parsed = Severity.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unrecognized severity value: '" + raw.trim() + "'");
        }
        if (parsed == NONE) {
            throw new ValidationException("severity NONE is not a storable alert severity");
        }
        return parsed;
    }
}
