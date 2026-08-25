package org.example;


public class SilentDefault_FIXED_06 {

    public static void main(String[] args) {

        System.out.println("=== Attempt 1: Same bug, but fail-secure default ===");
        // We keep the String version (with the same typo!) but change
        // the default branch to assume the WORST case, not the best.
        handleWithFailSecureDefault("hihg");

        System.out.println();
        System.out.println("=== Attempt 2: Enum makes the typo impossible ===");
        // With an enum, "hihg" isn't even a valid value we could assign —
        // this line intentionally would not compile if uncommented:
        // Severity typo = Severity.valueOf("HIHG"); // throws at runtime!
        //
        // Instead, let's show what happens with SAFE conversion:
        Severity parsed = safeParseSeverity("hihg");
        System.out.println("Safely parsed unrecognized input to: " + parsed);
    }

    // Fix #1: keep the String version, but flip the default's assumption.
    private static void handleWithFailSecureDefault(String severity) {
        switch (severity) {
            case "CRITICAL":
                System.out.println("Patch in 24h");
                break;
            case "HIGH":
                System.out.println("Patch in 7 days");
                break;
            default:
                // FAIL-SECURE: unrecognized input is treated as the
                // HIGHEST-scrutiny case, not "safe to ignore."
                System.out.println("ESCALATE - unrecognized severity value: \"" + severity + "\"");
                break;
        }
    }

    // Fix #2: an enum-based, fail-secure conversion helper. Unrecognized
    // text safely becomes a known "worst case" enum value instead of
    // crashing OR silently doing nothing.
    private static Severity safeParseSeverity(String rawInput) {
        try {
            return Severity.valueOf(rawInput.toUpperCase());
        } catch (IllegalArgumentException e) {
            // We haven't formally covered try/catch yet (that's later),
            // but this preview shows WHY it exists: Severity.valueOf()
            // throws when the text doesn't match any enum constant, and
            // we catch that here to fail secure instead of crashing.
            return Severity.CRITICAL; // treat unknown input as worst case
        }
    }

}

/*
 * EXPECTED OUTPUT:
 * === Attempt 1: Same bug, but fail-secure default ===
 * ESCALATE - unrecognized severity value: "hihg"
 *
 * === Attempt 2: Enum makes the typo impossible ===
 * Safely parsed unrecognized input to: CRITICAL
 *
 * EXPLANATION:
 *   Attempt 1 shows that even without fixing the typo, simply changing
 *   what the default branch DOES transforms this from a dangerous silent
 *   failure into a visible, actionable escalation. The typo is still
 *   there, but now it can't hide.
 *
 *   Attempt 2 previews Day 7 material (try/catch) intentionally — tell
 *   students they'll fully understand this mechanism in five days, but
 *   the important idea today is simpler: unknown input should resolve
 *   to the SAFEST assumption your system can make, never the most
 *   convenient one.
 */
