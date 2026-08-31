package org.example;/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 2
 * Vulnerability Triage Classifier
 * FILE: Severity.java
 * ============================================================
 *
 * TODO: Define an enum named Severity with at least 4 values
 * representing a fixed set of vulnerability severity levels.
 *
 * Suggested values (you may rename or add to these, but keep at
 * least 4 distinct levels): LOW, MEDIUM, HIGH, CRITICAL
 *
 * Reminder from lecture: an enum restricts a variable to a fixed,
 * named, compiler-checked set of values — this is what makes a typo
 * like "HIHG" impossible to compile, instead of a silent runtime bug.
 * ============================================================
 */
    // TODO: list your severity values here, separated by commas.
    // Example shape (do not just copy this — think about whether you
    // want exactly these four names or your own scheme):
    //
    //     LOW, MEDIUM, HIGH, CRITICAL

public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}




// I originally went with different names, but this seems to fit better with using with other assignments.
// My original names were SMALL, MEDIUM, LARGE, AND X_LARGE.

