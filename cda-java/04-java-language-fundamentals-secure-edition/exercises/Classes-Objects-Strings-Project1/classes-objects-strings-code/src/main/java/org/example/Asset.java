package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 5
 * Asset & Vulnerability Class Model
 * FILE 2 of 3: Asset.java
 * ============================================================
 *
 * TODO 1: Add at least two fields: assetName (String) and
 * owner (String). Feel free to add more fields if you'd like
 * (e.g., ipAddress, environment) — this is your design choice.
 *
 * TODO 2: Write a constructor that takes your fields as parameters and
 * assigns them using "this.fieldName = ...".
 *
 * TODO 3: Write an instance method isOwnedBy(String candidateOwner)
 * that returns true if candidateOwner matches this Asset's owner
 * field. Use .equals() to compare — NEVER == for String comparison,
 * per today's lecture (and today's misconception callout specifically).
 *
 * TODO 4: Write countCriticalVulnerabilities(Vulnerability[] findings)
 * that loops over the given array and returns an int count of how many
 * findings have isCritical() == true. This reuses the isCritical()
 * method you wrote on Vulnerability — you should NOT re-check severity
 * directly here.
 *
 * TODO 5: Write getTotalRemediationCost(Vulnerability[] findings) that
 * loops over the given array and returns a BigDecimal representing the
 * sum of every finding's estimatedRemediationCost. Start your running
 * total at BigDecimal.ZERO (a built-in constant) and use .add() to
 * accumulate — you CANNOT use + on BigDecimal values, it will not
 * compile.
 *
 * TODO 6: Write countOverdueVulnerabilities(Vulnerability[] findings)
 * that loops over the given array and returns an int count of how many
 * findings have isOverdue() == true. Just like TODO 4, reuse the
 * isOverdue() method you wrote on Vulnerability — do NOT recalculate
 * deadlines or compare dates directly here.
 *
 * NOTE: Asset does NOT need a custom equals() override for this lab
 * (though you're welcome to add toString() for your own convenience
 * when testing) — the required equals()/hashCode()/toString() overrides
 * are on Vulnerability only. Day 6 will expand this class further.
 * ============================================================
 */

import java.math.BigDecimal;

public class Asset {

    // TODO 1: declare your fields here.

    private String assetName;
    private String owner;
    private String location;

    // TODO 2: write your constructor here.

    public Asset(String assetName, String owner, String location) {
        this.assetName = assetName;
        this.owner = owner;
        this.location = location;
    }

    // TODO 3: write your isOwnedBy(String candidateOwner) method here.

    public boolean isOwnedBy(String candidateOwner) {
        return this.owner.equals(candidateOwner);
    }

    // TODO 4: write your countCriticalVulnerabilities(Vulnerability[] findings) method here.

    public int countCriticalVulnerabilities(Vulnerability[] findings) {
        int count = 0;

        for (Vulnerability finding : findings) {
            if (finding.isCritical()) {
                count++;
            }
        }

        return count;
    }

    // TODO 5: write your getTotalRemediationCost(Vulnerability[] findings) method here.

    public BigDecimal getTotalRemediationCost(Vulnerability[] findings) {
        BigDecimal total = BigDecimal.ZERO;

        for (Vulnerability finding : findings) {
            total = total.add(finding.getEstimatedRemediationCost());
        }

        return total;
    }

    // TODO 6: write your countOverdueVulnerabilities(Vulnerability[] findings) method here.

    public int countOverdueVulnerabilities(Vulnerability[] findings) {
        int count = 0;

        for (Vulnerability finding : findings) {
            if (finding.isOverdue()) {
                count++;
            }
        }

        return count;
    }

    public String getAssetName() {
        return this.assetName;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getLocation() {
        return this.location;
    }
}

