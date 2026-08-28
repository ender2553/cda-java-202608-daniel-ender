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


    // TODO 2: write your constructor here.


    // TODO 3: write your isOwnedBy(String candidateOwner) method here.


    // TODO 4: write your countCriticalVulnerabilities(Vulnerability[] findings) method here.


    // TODO 5: write your getTotalRemediationCost(Vulnerability[] findings) method here.


    // TODO 6: write your countOverdueVulnerabilities(Vulnerability[] findings) method here.


}
