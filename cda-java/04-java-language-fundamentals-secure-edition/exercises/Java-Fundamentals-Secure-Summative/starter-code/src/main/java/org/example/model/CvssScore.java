package org.example.model;

import java.util.Objects;

/**
 * Immutable CVSS v3.1 base score for a single CVE.
 * REFERENCE EXAMPLE -- fully implemented, no TODOs here.
 */
public final class CvssScore {

    private final String cveId;
    private final double baseScore; // must be in [0.0, 10.0]
    private final String severityLabel; // NONE, LOW, MEDIUM, HIGH, CRITICAL

    public CvssScore(String cveId, double baseScore, String severityLabel) {
        this.cveId = Objects.requireNonNull(cveId, "cveId");
        this.baseScore = baseScore;
        this.severityLabel = Objects.requireNonNull(severityLabel, "severityLabel");
    }

    public String getCveId() { return cveId; }
    public double getBaseScore() { return baseScore; }
    public String getSeverityLabel() { return severityLabel; }

    @Override
    public String toString() {
        return "CvssScore{" + cveId + ", base=" + baseScore + ", severity=" + severityLabel + "}";
    }
}
