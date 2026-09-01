package org.example;
import java.math.BigDecimal;

/**
 * An organizational asset, carried forward unchanged from Day 5.
 */
public class Asset {

    private final String assetName;
    private final String owner;

    public Asset(String assetName, String owner) {
        this.assetName = assetName;
        this.owner = owner;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isOwnedBy(String candidateOwner) {
        return this.owner.equals(candidateOwner);
    }

    public int countCriticalVulnerabilities(Vulnerability[] vulnerabilities) {
        int count = 0;
        for (Vulnerability v : vulnerabilities) {
            if (v.isCritical()) {
                count++;
            }
        }
        return count;
    }

    public BigDecimal getTotalRemediationCost(Vulnerability[] vulnerabilities) {
        BigDecimal total = BigDecimal.ZERO;
        for (Vulnerability v : vulnerabilities) {
            total = total.add(v.getEstimatedRemediationCost());
        }
        return total;
    }
}
