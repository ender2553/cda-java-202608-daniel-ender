/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 2
 * Vulnerability Triage Classifier
 * FILE: TriageClassifier.java
 * ============================================================
 *
 * SCENARIO:
 *   The calculator produces a number, but analysts don't agree
 *   on what to DO with that number. Your manager wants consistent
 *   triage decisions: given a severity, the tool should tell the analyst
 *   the remediation SLA and recommended action.
 *
 * REQUIREMENTS (this is what's graded — see rubric at the bottom):
 *   1. Define a Severity enum with at least 4 values (see Severity.java).
 *   2. Use a switch on the enum to map severity to an SLA/action.
 *   3. Accept input (Scanner or args) and convert it to the enum safely.
 *   4. Handle invalid/unrecognized input with a FAIL-SECURE default —
 *      NOT a fail-open one. Review 06_SilentDefault_FIXED.java if
 *      you're unsure what this means.
 *   5. Print a clear recommendation, not just a raw score.
 *
 * RULES:
 *   - This is INDIVIDUAL work. Do not copy from a classmate.
 *   - You may use your Day 2 code-along files as reference.
 *   - Test your program with a VALID severity, an INVALID/misspelled
 *     severity, and (if using Scanner) empty input, before submitting.
 *
 * SUBMISSION:
 *   Save both files (Severity.java and this file) and submit them
 *   together, along with output from at least 3 test runs: one valid
 *   severity, one invalid/misspelled input, and your choice of a third.
 * ============================================================
 */

import java.util.Scanner;

public class TriageClassifier {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        // ------------------------------------------------------------
        // TODO 1: Prompt the analyst to type in a severity level as
        // text (e.g., "HIGH"). Read it with input.nextLine() and store
        // it in a String variable. Remember: Scanner input is always
        // text first — you'll convert it to your enum in TODO 2.
        //
        // Example prompt: "Enter severity (LOW, MEDIUM, HIGH, CRITICAL): "
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 2: Safely convert the text the analyst typed into a
        // Severity enum value. You have two reasonable approaches —
        // pick ONE:
        //
        //   Approach A (try/catch preview — optional, more advanced):
        //     Use Severity.valueOf(rawInput.toUpperCase()) inside a
        //     try/catch block, similar to safeParseSeverity() in
        //     06_SilentDefault_FIXED.java. If it throws, fall back to
        //     a fail-secure default value.
        //
        //   Approach B (if/else chain — matches what we've fully
        //   covered so far):
        //     Manually compare the text against each valid value with
        //     .equalsIgnoreCase(), assigning the matching enum constant.
        //     If nothing matches, assign your fail-secure default.
        //
        // Either approach is acceptable. Store the result in a
        // Severity-typed variable.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 3: Use a switch statement on your Severity variable to
        // print a remediation SLA and recommended action for each
        // possible value. Use arrow syntax (case X -> ...) as shown in
        // lecture. Make sure every enum value you defined has a case.
        //
        // Example shape:
        //   switch (severity) {
        //       case CRITICAL -> System.out.println("Patch within 24 hours");
        //       case HIGH     -> System.out.println("Patch within 7 days");
        //       // ... etc for every value in your enum
        //   }
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 4 (CHECK YOUR FAIL-SECURE LOGIC):
        // Re-read your TODO 2 code. If the analyst types something
        // unrecognized (e.g., "hihg" or "banana"), does your program:
        //   (a) crash unhelpfully,
        //   (b) silently do nothing / assume it's safe (FAIL-OPEN — bad!), or
        //   (c) clearly escalate / flag it for review (FAIL-SECURE — good!)
        //
        // If your answer is (a) or (b), revise TODO 2 before submitting.
        // This is worth real rubric points — don't skip this check.
        // ------------------------------------------------------------


        input.close();
    }

}

/*
 * ============================================================
 * GRADING RUBRIC (100 points total)
 * ============================================================
 *   20 pts — Correct, idiomatic enum usage (Severity.java has 4+ values,
 *            used consistently as the type throughout)
 *   20 pts — Switch/branch logic correctness (every enum value has a
 *            matching case with a sensible SLA/action)
 *   25 pts — Fail-secure (not fail-open) default behavior: unrecognized
 *            input must escalate/flag, not silently proceed as if safe
 *   20 pts — Clean, readable guard-clause style validation (input
 *            handling is easy to follow, not deeply nested)
 *   15 pts — Code compiles and runs without errors across all 3 required
 *            test cases (valid, invalid, and your third case)
 * ============================================================
 */
