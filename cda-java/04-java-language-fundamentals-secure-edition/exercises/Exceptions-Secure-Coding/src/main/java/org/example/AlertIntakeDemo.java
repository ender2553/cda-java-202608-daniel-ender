package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 6
 * Secure Alert Intake Toolkit
 * FILE 7 of 7: AlertIntakeDemo.java (your test/demo driver)
 * ============================================================
 *
 * SCENARIO:
 *   Your manager wants a real alert intake tool: reject malformed
 *   data instead of guessing, keep the screen clean when something
 *   fails, and never let outside code quietly corrupt the internal
 *   alert list. Today you'll build exactly that, combining
 *   everything from this morning and this afternoon.
 *
 * REQUIREMENTS (this is what's graded — see rubric at the bottom):
 *   1. Defensive parsing: allow-list severity, anchored regex CVE
 *      format, safe numeric score parsing, fail-closed on anything
 *      malformed.
 *   2. A List<Alert> for every accepted alert, and a Set<String> for
 *      unique CVE IDs.
 *   3. A custom exception type; specific catches only, no broad or
 *      empty catches.
 *   4. Generic user-facing messages; full detail only in an internal
 *      log.
 *   5. Constructor and getters use defensive copying / unmodifiable
 *      views — no leaked internal references.
 *
 * RULES:
 *   - This is INDIVIDUAL work. Do not copy from a classmate.
 *
 * SUBMISSION:
 *   Submit all 7 files, along with output demonstrating every
 *   required behavior below.
 *
 * TODO 1: Create an AlertIntake instance.
 *
 * TODO 2: Open an AlertFeedConnection using try-with-resources (the
 * try(...) form) so it's guaranteed to close automatically. Inside the
 * try block, loop over feed.fetchRawLines(). For each raw line, call
 * intake.addAlert(rawLine) inside its OWN try/catch that catches
 * SPECIFICALLY InvalidAlertException (never a bare Exception). In the
 * catch block, implement the two-surface pattern from this morning:
 *   - Generate a short reference ID (e.g.,
 *     UUID.randomUUID().toString().substring(0, 8))
 *   - Print a DETAILED line prefixed with something like "[SERVER LOG]"
 *     including the reference ID, e.constructor's message, and the
 *     raw line — this simulates what would go to a real internal log
 *   - Print a GENERIC line shown to "the outside world" containing
 *     ONLY a polite message and the same reference ID — nothing else
 *
 * TODO 3: After the try-with-resources block, print a summary:
 *   - intake.getAllAlerts().size()
 *   - intake.getUniqueCveIds()
 *   - intake.countBySeverity(Severity.HIGH)
 *   - intake.countBySeverity(Severity.CRITICAL)
 *
 * TODO 4: Demonstrate your defensive encapsulation actually works:
 * call intake.getAllAlerts() and attempt to .add(null) to the result
 * inside a try/catch for UnsupportedOperationException, printing
 * confirmation if it's correctly rejected. Do the same for
 * intake.getUniqueCveIds() attempting to .add("HACKED").
 * ============================================================
 */
import java.util.UUID;

public class AlertIntakeDemo {

    public static void main(String[] args) {

        // TODO 1: create your AlertIntake here.

        AlertIntake intake = new AlertIntake();

        // TODO 2: open the feed with try-with-resources, loop over
        // fetchRawLines(), and handle each addAlert() call with the
        // two-surface pattern described above.

        try (AlertFeedConnection feed = new AlertFeedConnection("SecurityFeed")) {

            for (String rawLine : feed.fetchRawLines()) {

                try {
                    intake.addAlert(rawLine);

                } catch (InvalidAlertException e) {
                    String referenceId =
                            UUID.randomUUID().toString().substring(0, 8);

                    System.out.println(
                            "[SERVER LOG] Ref=" + referenceId
                                    + " Error=" + e.getMessage()
                                    + " RawLine=" + rawLine
                    );

                    System.out.println(
                            "Sorry, we couldn't process that alert. "
                                    + "Reference ID: " + referenceId
                    );
                }
            }
        }

        // TODO 3: print your summary here.

        System.out.println("Total accepted alerts: " + intake.getAllAlerts().size());
        System.out.println("Unique CVE IDs: " + intake.getUniqueCveIds());
        System.out.println("HIGH alerts: " + intake.countBySeverity(Severity.HIGH));
        System.out.println("CRITICAL alerts: " + intake.countBySeverity(Severity.CRITICAL));


        // TODO 4: demonstrate defensive encapsulation here.

        try {
            intake.getAllAlerts().add(null);
        } catch (UnsupportedOperationException e) {
            System.out.println("Confirmed: alert list cannot be modified.");
        }

        try {
            intake.getUniqueCveIds().add("HACKED");
        } catch (UnsupportedOperationException e) {
            System.out.println("Confirmed: CVE ID set cannot be modified.");
        }
    }

}









/*
 * ============================================================
 * GRADING RUBRIC (100 points total)
 * ============================================================
 *   20 pts — Defensive parsing correctness: allow-list severity
 *            (via enum), anchored CVE regex, safe numeric parsing
 *            with range check, length caps, all fail-closed
 *   15 pts — Collections correctness: List preserves order/duplicates,
 *            Set correctly deduplicates, countBySeverity() is accurate
 *   20 pts — Exception handling discipline: custom exception type,
 *            SPECIFIC catches only (never broad/empty), correct
 *            try-with-resources usage
 *   15 pts — Safe error surfaces: generic message shown outward,
 *            full detail (with matching reference ID) only in the
 *            internal log simulation
 *   20 pts — Memory & reference security: Alert is a genuine immutable
 *            value type (final class, final fields, no setters); both
 *            getters on AlertIntake return unmodifiable views, never
 *            the raw internal collections
 *   10 pts — Code compiles and runs without errors, correctly
 *            producing 3 accepted alerts and 4 rejected alerts from
 *            the provided test data
 * ============================================================
 */
