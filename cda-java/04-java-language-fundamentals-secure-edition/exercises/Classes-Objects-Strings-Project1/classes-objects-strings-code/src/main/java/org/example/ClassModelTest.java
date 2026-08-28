package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 5
 * Asset & Vulnerability Class Model
 * FILE 4: ClassModelTest.java (your test/demo driver)
 * ============================================================
 *
 * SCENARIO:
 *   The 2D array from Day 3 was fragile and error-prone. Today you'll
 *   replace it with real, named classes — the exact data model Day 6's
 *   SBOM inventory tool will build on.
 *
 * REQUIREMENTS (this is what's graded — see rubric at the bottom):
 *   1. Vulnerability class with 6 fields (including BigDecimal cost
 *      and LocalDate discoveredDate), a constructor, and instance
 *      methods isCritical(), getRemediationDeadline(), isOverdue().
 *   2. Asset class with isOwnedBy(String) using .equals().
 *   3. Asset.countCriticalVulnerabilities(Vulnerability[]) — loops
 *      over an array of Vulnerability objects.
 *   4. Asset.getTotalRemediationCost(Vulnerability[]) — loops over an
 *      array and returns a BigDecimal total built with .add(), never +.
 *   5. Asset.countOverdueVulnerabilities(Vulnerability[]) — loops over
 *      an array and counts findings whose isOverdue() is true.
 *   6. Override equals()/toString() on Vulnerability, comparing by
 *      cveId only.
 *   7. Use .equals() (never ==) for all String content comparisons in
 *      this file.
 *
 * RULES:
 *   - This is INDIVIDUAL work. Do not copy from a classmate.
 *   - Keep these files after submission — Day 6 builds directly on them.
 *
 * SUBMISSION:
 *   Submit Vulnerability.java, Asset.java, Severity.java, and this file,
 *   along with output demonstrating every required behavior below.
 * ============================================================
 */

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClassModelTest {

    public static void main(String[] args) {

        // ------------------------------------------------------------
        // TODO 1: Create at least 3 Vulnerability objects using your
        // constructor, including a real BigDecimal cost (e.g.,
        // new BigDecimal("1500.00")) and a real LocalDate discoveredDate
        // for each. Make TWO of them share the SAME cveId (to test your
        // equals() override) but differ in at least one other field.
        // At least one should have Severity.CRITICAL so your
        // isCritical() tests below have something to find.
        //
        // For discoveredDate, make at least ONE finding clearly OLD
        // (e.g., LocalDate.of(2026, 1, 15)) so it will be overdue no
        // matter when you run this, and at least ONE discovered
        // LocalDate.now() so it will NOT be overdue yet.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 2: Print each Vulnerability object directly (e.g.,
        // System.out.println(finding1);) to demonstrate your
        // toString() override produces readable output, including the
        // cost and discovery date, not a default memory-address-style
        // string.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 3: Call .equals() between your two same-cveId objects
        // and print the result (should be true). Call .equals()
        // between two different-cveId objects and print that result
        // too (should be false).
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 4: Call isCritical() on at least two of your
        // Vulnerability objects (one CRITICAL, one not) and print both
        // results.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 5: Call getRemediationDeadline() and isOverdue() on your
        // old finding and your freshly-discovered finding, and print
        // all four results. The old finding's isOverdue() should print
        // true; the fresh finding's should print false.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 6: Create at least one Asset object using your
        // constructor, and print its fields to confirm it was built
        // correctly.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 7: Call isOwnedBy() on your Asset with a String that
        // DOES match its owner, and print the result (should be true).
        // Call it again with a String that does NOT match, and print
        // that result too (should be false).
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 8: Put your Vulnerability objects from TODO 1 into a
        // Vulnerability[] array. Call countCriticalVulnerabilities(),
        // getTotalRemediationCost(), and countOverdueVulnerabilities()
        // on your Asset with that array, and print all three results.
        // ------------------------------------------------------------


    }

}

/*
 * ============================================================
 * GRADING RUBRIC (100 points total)
 * ============================================================
 *   15 pts — Correct class/constructor design for BOTH Vulnerability
 *            (including BigDecimal cost and LocalDate discoveredDate
 *            fields) and Asset
 *   20 pts — Correct equals() semantics: compares by cveId using
 *            .equals() internally, returns true/false correctly for
 *            same-cveId vs. different-cveId objects
 *   10 pts — Readable toString() output including cost and discovery
 *            date (not default Object.toString() memory-address style)
 *   20 pts — Instance methods correctly use object state: isCritical(),
 *            isOwnedBy() (using .equals(), never ==),
 *            getRemediationDeadline() (correct switch-based day math),
 *            and isOverdue() (reusing getRemediationDeadline(), using
 *            .isAfter() correctly)
 *   20 pts — Array-processing methods: countCriticalVulnerabilities(),
 *            getTotalRemediationCost(), and countOverdueVulnerabilities()
 *            all loop correctly over a Vulnerability[] array and return
 *            correct results
 *   10 pts — BigDecimal and LocalDate handled correctly: BigDecimal
 *            constructed from Strings and combined only with .add();
 *            LocalDate combined only with .plusDays()/.isAfter(),
 *            never == or arithmetic operators
 *   5 pts  — Code compiles and runs without errors, demonstrating
 *            every required behavior in the test output
 * ============================================================
 */
