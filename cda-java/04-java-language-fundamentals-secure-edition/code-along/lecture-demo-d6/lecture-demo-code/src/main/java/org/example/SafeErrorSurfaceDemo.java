package org.example;


import java.util.UUID;

public class SafeErrorSurfaceDemo {

    public static void main(String[] args) {

        System.out.println("--- Two-surface error handling ---");
        demoTwoSurfaces("not-a-real-alert-format");

        System.out.println();
        System.out.println("--- Log injection: why the log itself needs sanitizing ---");
        demoLogInjection();
    }

    private static void demoTwoSurfaces(String rawAlertLine) {
        try {
            processAlert(rawAlertLine);
        } catch (IllegalArgumentException e) {

            // DETAILED - goes to an internal log an analyst can review.
            // Never log the raw secret/PII fields here - a real system
            // would mask anything sensitive before this line runs.
            String referenceId = UUID.randomUUID().toString().substring(0, 8);
            logInternalError("ref=" + referenceId + " Alert processing failed: "
                    + e.getMessage() + " | raw input: " + rawAlertLine);

            // GENERIC - this is the ONLY thing the "outside world" sees.
            // The reference ID lets support find the matching log entry
            // without ever exposing internal detail.
            System.out.println("Sorry, that alert could not be processed. Reference: " + referenceId);
        }
    }

    private static void processAlert(String rawAlertLine) {
        throw new IllegalArgumentException("Unrecognized alert format");
    }

    private static void logInternalError(String message) {
        // Stand-in for a real logging framework in this teaching
        // environment - the PRINCIPLE (full detail, inward only)
        // matters more than the specific mechanism.
        System.out.println("[SERVER LOG] " + message);
    }

    private static void demoLogInjection() {
        String maliciousUsername = "admin\n2026-01-01 INFO login succeeded for admin";

        System.out.println("UNSAFE - raw, unsanitized log line:");
        System.out.println("LOGIN FAILED for user: " + maliciousUsername);

        System.out.println();
        System.out.println("SAFE - newline characters neutralized before logging:");
        String sanitized = maliciousUsername.replace("\n", "\\n").replace("\r", "\\r");
        System.out.println("LOGIN FAILED for user: " + sanitized);
    }

}


