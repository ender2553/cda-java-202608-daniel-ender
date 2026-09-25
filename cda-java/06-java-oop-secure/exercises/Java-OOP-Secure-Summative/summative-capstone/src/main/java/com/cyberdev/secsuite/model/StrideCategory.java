package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Locale;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * The six STRIDE threat categories (Microsoft's threat-modeling mnemonic). Values match the
 * CHECK constraint on threat_model_entry.stride_category exactly.
 *
 * Each category is the violation of one security property; {@link #violatedProperty()} makes
 * that mapping explicit and is used as a column in the report's STRIDE coverage table.
 */
public enum StrideCategory {
    SPOOFING,
    TAMPERING,
    REPUDIATION,
    INFORMATION_DISCLOSURE,
    DENIAL_OF_SERVICE,
    ELEVATION_OF_PRIVILEGE;

    /**
     * The security property this category of threat violates. Written as an exhaustive
     * switch EXPRESSION with no default branch: if a seventh constant were ever added to this
     * enum, this method would stop compiling until someone decided what property it violates
     * -- the compiler enforces completeness instead of a silent default. Same idiom SEC-8's
     * coverage method and SEC-5's RiskAssessment switch rely on.
     */
    public String violatedProperty() {
        return switch (this) {
            case SPOOFING -> "Authentication";
            case TAMPERING -> "Integrity";
            case REPUDIATION -> "Non-repudiation";
            case INFORMATION_DISCLOSURE -> "Confidentiality";
            case DENIAL_OF_SERVICE -> "Availability";
            case ELEVATION_OF_PRIVILEGE -> "Authorization";
        };
    }

    /** Human-readable label, e.g. INFORMATION_DISCLOSURE -> "Information Disclosure". */
    public String displayName() {
        return switch (this) {
            case SPOOFING -> "Spoofing";
            case TAMPERING -> "Tampering";
            case REPUDIATION -> "Repudiation";
            case INFORMATION_DISCLOSURE -> "Information Disclosure";
            case DENIAL_OF_SERVICE -> "Denial of Service";
            case ELEVATION_OF_PRIVILEGE -> "Elevation of Privilege";
        };
    }

    /** Strict allow-list parse; see Criticality.parse for the rationale. */
    public static StrideCategory parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("StrideCategory must not be blank");
        }
        try {
            return StrideCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unrecognized StrideCategory value: '" + raw.trim() + "'");
        }
    }
}
