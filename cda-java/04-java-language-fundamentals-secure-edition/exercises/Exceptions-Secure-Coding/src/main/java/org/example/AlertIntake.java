package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 6
 * Secure Alert Intake Toolkit
 * FILE 6 of 7: AlertIntake.java
 * ============================================================
 *
 * TODO 1: Declare two private final fields:
 *   - a List<Alert> to hold every ACCEPTED alert, in the order
 *     received, duplicates included (order and repeats are
 *     meaningful data — this is why it's a List, not a Set)
 *   - a Set<String> to hold only the DISTINCT CVE IDs seen
 * Initialize both to empty ArrayList/HashSet instances.
 *
 * TODO 2: Write addAlert(String rawLine) that calls
 * AlertParser.parseAlertLine(rawLine) and adds the resulting Alert to
 * BOTH your List and your Set (add the alert's cveId to the Set).
 * IMPORTANT: do NOT catch the exception AlertParser might throw here —
 * let InvalidAlertException propagate uncaught to whoever calls
 * addAlert(). If parsing fails, NOTHING should be added to either
 * collection. Deciding what to DO about a rejected alert (the
 * generic-message / detailed-log pattern) is the CALLER's job, not
 * this class's.
 *
 * TODO 3: Write getAllAlerts() returning a List<Alert>. Do NOT return
 * your internal list directly — wrap it with
 * Collections.unmodifiableList(...) so external code can read it but
 * never mutate your internal state through the returned reference.
 *
 * TODO 4: Write getUniqueCveIds() the same way, using
 * Collections.unmodifiableSet(...).
 *
 * TODO 5: Write countBySeverity(Severity target) that loops over your
 * internal alert list and returns an int count of how many alerts
 * have that exact severity.
 * ============================================================
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlertIntake {

    // TODO 1: declare your two fields here.

    private final List<Alert> acceptedAlerts = new ArrayList<>();
    private final Set<String> distinctCveIds = new HashSet<>();

    // TODO 2: write addAlert(String rawLine) here.

    public void addAlert(String rawLine) {
        AlertParser parser = new AlertParser();
        Alert alert = parser.parseAlertLine(rawLine);

        acceptedAlerts.add(alert);
        distinctCveIds.add(alert.getCveId());
    }

    // TODO 3: write getAllAlerts() here.

    public List<Alert> getAllAlerts() {
        return Collections.unmodifiableList(acceptedAlerts);
    }

    // TODO 4: write getUniqueCveIds() here.

    public Set<String> getUniqueCveIds() {
        return Collections.unmodifiableSet(distinctCveIds);
    }

    // TODO 5: write countBySeverity(Severity target) here.

    public int countBySeverity(Severity target) {
        int count = 0;

        for (Alert alert : acceptedAlerts) {
            if (alert.getSeverity() == target) {
                count++;
            }
        }

        return count;
    }

}
