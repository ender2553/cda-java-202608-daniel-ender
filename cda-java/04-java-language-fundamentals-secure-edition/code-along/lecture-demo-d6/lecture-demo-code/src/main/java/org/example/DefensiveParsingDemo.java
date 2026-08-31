package org.example;


import java.util.HashSet;
import java.util.Set;

public class DefensiveParsingDemo {

    // Recall from Day 2: an enum is itself an allow-list, enforced by
    // the compiler. Severity.valueOf("BOGUS") throws automatically -
    // no manual Set needed for this specific case.
    enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    static final Set<String> ALLOWED_SEVERITIES_MANUAL =
            new HashSet<>(Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL"));

    static final int MAX_CVE_LENGTH = 20;

    public static void main(String[] args) {

        System.out.println("--- Allow-list via enum (Day 2 callback) ---");
        demoEnumAllowList();

        System.out.println();
        System.out.println("--- Allow-list via manual Set (for non-enum cases) ---");
        demoManualAllowList();

        System.out.println();
        System.out.println("--- Length cap, checked BEFORE anything else ---");
        demoLengthCap();

        System.out.println();
        System.out.println("--- Safe numeric parsing, fail-closed ---");
        demoSafeNumericParsing();
    }

    private static void demoEnumAllowList() {
        System.out.println("Valid: " + parseeSeverityEnum("HIGH"));
        try {
            parseeSeverityEnum("SUPER_HIGH");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }

    private static Severity parseeSeverityEnum(String raw) {
        return Severity.valueOf(raw); // throws IllegalArgumentException on any unlisted value
    }

    private static void demoManualAllowList() {
        System.out.println("Valid: " + validateSeverityManual("high"));
        try {
            validateSeverityManual("HIGH!!");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }

    private static String validateSeverityManual(String raw) {
        String normalized = raw.trim().toUpperCase();
        if (!ALLOWED_SEVERITIES_MANUAL.contains(normalized)) {
            // FAIL CLOSED - reject outright. Do NOT try to strip the
            // "!!" and accept "HIGH" anyway; that's coercion, not validation.
            throw new IllegalArgumentException("\"" + raw + "\" is not an allowed severity");
        }
        return normalized;
    }

    private static void demoLengthCap() {
        System.out.println("Valid: " + validateCveId("CVE-2024-1234"));
        try {
            validateCveId("CVE-2024-1234-WITH-WAY-TOO-MUCH-EXTRA-TEXT-APPENDED");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }

    private static String validateCveId(String raw) {
        if (raw.length() > MAX_CVE_LENGTH) {
            // Length is checked FIRST, before any regex or storage work.
            throw new IllegalArgumentException("CVE ID exceeds " + MAX_CVE_LENGTH + " characters");
        }
        return raw;
    }

    private static void demoSafeNumericParsing() {
        System.out.println("Valid: " + parseScoreSafely("7.5"));
        try {
            parseScoreSafely("seven point five");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected (not a number): " + e.getMessage());
        }
        try {
            parseScoreSafely("999");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected (out of range): " + e.getMessage());
        }
    }

    private static double parseScoreSafely(String input) {
        try {
            double score = Double.parseDouble(input.trim());
            if (score < 0.0 || score > 10.0) {
                throw new IllegalArgumentException("Score out of range: " + score);
            }
            return score;
        } catch (NumberFormatException e) {
            // FAIL CLOSED - a parse failure becomes a clean rejection,
            // never a silent default like 0.0.
            throw new IllegalArgumentException("Not a valid number: \"" + input + "\"");
        }
    }

}


