package org.example.model;

import java.util.Objects;

/**
 * Immutable EPSS (Exploit Prediction Scoring System) record for a CVE.
 * REFERENCE EXAMPLE -- fully implemented, no TODOs here.
 *
 * probability: FIRST.org's estimated probability [0.0, 1.0] that this CVE
 *              will be exploited in the wild in the next 30 days.
 * percentile:  where this CVE ranks [0.0, 1.0] relative to all scored CVEs.
 */
public final class EpssScore {

    private final String cveId;
    private final double probability;
    private final double percentile;

    public EpssScore(String cveId, double probability, double percentile) {
        this.cveId = Objects.requireNonNull(cveId, "cveId");
        this.probability = probability;
        this.percentile = percentile;
    }

    public String getCveId() { return cveId; }
    public double getProbability() { return probability; }
    public double getPercentile() { return percentile; }

    @Override
    public String toString() {
        return "EpssScore{" + cveId + ", probability=" + probability + ", percentile=" + percentile + "}";
    }
}
