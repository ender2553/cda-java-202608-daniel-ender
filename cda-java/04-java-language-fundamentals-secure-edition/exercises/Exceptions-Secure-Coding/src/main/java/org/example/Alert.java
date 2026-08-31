package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 6
 * Secure Alert Intake Toolkit
 * FILE 3 of 7: Alert.java
 * ============================================================
 *
 * TODO 1: Declare the class as "public final class Alert" — final
 * means no subclass can add mutable state later, part of the full
 * immutable-value-type recipe from today's lecture.
 *
 * TODO 2: Add four private final fields: cveId (String), component
 * (String), severity (Severity), score (double).
 *
 * TODO 3: Write a constructor that sets all four fields. Do NOT
 * re-validate the inputs here — trust that whoever calls this
 * constructor (you'll build that in AlertParser) already validated
 * everything at the trust boundary. Re-checking here would be
 * redundant, not extra-safe.
 *
 * TODO 4: Write simple getters for all four fields (getCveId(),
 * getComponent(), getSeverity(), getScore()) — no setters. This class
 * should have NO way to change its state after construction.
 *
 * TODO 5: Override equals() and hashCode(), comparing by cveId ONLY —
 * same pattern as Day 5's Vulnerability class. Two Alert records about
 * the same CVE are "the same alert" for comparison purposes, even if
 * component/severity/score differ.
 *
 * TODO 6: Override toString() for a readable one-line summary, e.g.,
 * "CVE-2024-1234 [openssl] - HIGH (7.5)".
 * ============================================================
 */
import java.util.Objects;

public final class Alert {

    // TODO 2: declare your four fields here.


    // TODO 3: write your constructor here.


    // TODO 4: write your four getters here.


    // TODO 5: override equals() and hashCode() here.


    // TODO 6: override toString() here.


}
