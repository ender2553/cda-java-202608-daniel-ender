package org.example;
/*
 * ============================================================
 * DAY 4 — SUPPLEMENTAL CODE-ALONG
 * ============================================================
 *
 *
 *   1. Separate "calculate" methods that each validate their own
 *      single input and throw on invalid data.
 *   2. A "combine" method that trusts its inputs were already
 *      validated (it does NOT re-check them).
 *   3. A "label" method that maps a final score to a category with
 *      NO gaps.
 *   4. An overloaded version of one method.
 *   5. A Javadoc comment (@param / @return / @throws) on every
 *      method you write — just like the lab requires.
 *
 * Fill in each TODO as we go. Don't worry about getting the exact
 * wording of error messages right — focus on the STRUCTURE.
 */

public class PatchPriorityCodeAlong {

    public static void main(String[] args) {

        System.out.println("=== Step 1: The Problem (monolithic, repeated logic) ===");
        //demoMonolithicVersion();

        System.out.println();
        System.out.println("=== Step 2: The Fix (decomposed, validated methods) ===");

        // ---- Happy path: two-factor version ----
       // double urgency = calculateUrgency(8.0);
       // double effort = calculateEffort(3.0);
       // double priority = calculatePatchPriority(urgency, effort);
       // String label = getPriorityLabel(priority);
       //System.out.printf("Two-factor priority: %.1f (%s)%n", priority, label);

        // ---- Happy path: three-factor OVERLOADED version ----
        //double businessImpact = calculateBusinessImpact(9.0);
        //double priorityWithImpact = calculatePatchPriority(urgency, effort, businessImpact);
        //String labelWithImpact = getPriorityLabel(priorityWithImpact);
        //System.out.printf("Three-factor priority: %.1f (%s)%n", priorityWithImpact, labelWithImpact);

        //System.out.println();
        //System.out.println("=== Step 3: Validation Actually Works ===");
        // ------------------------------------------------------------
        // TODO 1: Call calculateUrgency(-5.0) inside a try/catch block.
        // In the catch block (catching IllegalArgumentException),
        // print a message confirming the invalid input was rejected,
        // including e.getMessage().
        //
        // Example shape:
//           try {
//               calculateUrgency(-5.0);
//           } catch (IllegalArgumentException e) {
//               System.out.println("Correctly rejected invalid input: " + e.getMessage());
//           }
         //------------------------------------------------------------


    // ------------------------------------------------------------
    // STEP 1: THE PROBLEM (already written - just run it and review)
    // ------------------------------------------------------------
    // This is what NOT to do: the same averaging arithmetic, copy-
    // pasted for two different "systems." No validation anywhere.
    // DISCUSS: if this formula had a bug, how many places would we
    // need to fix it?
    //private static void demoMonolithicVersion() {
        double system1Urgency = 8.0;
        double system1Effort = 3.0;
        double system1Priority = (system1Urgency + system1Effort) / 2.0; // no validation!

        double system2Urgency = 4.0;
        double system2Effort = 9.0;
        double system2Priority = (system2Urgency + system2Effort) / 2.0; // same formula, copy-pasted

        System.out.println("System 1 priority (no validation): " + system1Priority);
        System.out.println("System 2 priority (no validation): " + system2Priority);
    }

    // ------------------------------------------------------------
    // TODO 2: Write calculateUrgency(double rawValue)
    // ------------------------------------------------------------
    // Requirements:
    //   - Return type: double
    //   - If rawValue is less than 0.0 OR greater than 10.0, throw:
    //       new IllegalArgumentException("Urgency must be between 0 and 10, got: " + rawValue)
    //   - Otherwise, return rawValue unchanged
    //   - Give it a Javadoc comment with @param, @return, and @throws
    //
    // Example shape:
    //   /**
    //    * Validates and returns an urgency value.
    //    *
    //    * @param rawValue the raw urgency input, expected to be 0-10
    //    * @return the validated urgency value, unchanged
    //    * @throws IllegalArgumentException if rawValue is outside 0-10
    //    */
    //   public static double calculateUrgency(double rawValue) {
    //       // your code here
    //   }


    // ------------------------------------------------------------
    // TODO 3: Write calculateEffort(double rawValue)
    // ------------------------------------------------------------
    // Same structure as calculateUrgency(), but validate/label it as
    // "Effort" instead of "Urgency" in the exception message. Give it
    // its own Javadoc comment too — don't just copy calculateUrgency()'s
    // and forget to update the wording.
    //
    // public static double calculateEffort(double rawValue) {
    //     // your code here


    // }


    // ------------------------------------------------------------
    // TODO 4: Write calculateBusinessImpact(double rawValue)
    // ------------------------------------------------------------
    // Same structure again, validating/labeling it as "Business impact".
    // This one will be used by the OVERLOADED calculatePatchPriority()
    // below in TODO 6. Javadoc comment required here too.
    //
    // public static double calculateBusinessImpact(double rawValue) {
    //     // your code here
    // }


    // ------------------------------------------------------------
    // TODO 5: Write calculatePatchPriority(double urgency, double effort)
    // ------------------------------------------------------------
    // IMPORTANT: this method should NOT re-validate urgency or effort.
    // Trust that calculateUrgency() and calculateEffort() already did
    // that job. This method's ONLY responsibility is the math.
    //
    // Formula (simplified for practice): the average of the two values.
    //
    // Javadoc note: this method needs @param and @return, but NOT
    // @throws — it can't throw, since it doesn't validate anything.
    //
    // public static double calculatePatchPriority(double urgency, double effort) {
    //     // your code here
    // }


    // ------------------------------------------------------------
    // TODO 6: Write an OVERLOADED calculatePatchPriority() that takes
    // THREE doubles instead of two: urgency, effort, businessImpact.
    // ------------------------------------------------------------
    // Same method NAME as TODO 5, but a DIFFERENT parameter list. Java
    // will pick the right version automatically based on how many
    // arguments are passed at the call site.
    //
    // Formula (simplified for practice): the average of all three values.
    //
    // Javadoc note: this overload needs its OWN complete Javadoc
    // comment — it does NOT inherit the two-parameter version's.
    //
    // public static double calculatePatchPriority(double urgency, double effort, double businessImpact) {
    //     // your code here
    // }


    // ------------------------------------------------------------
    // TODO 7: Write getPriorityLabel(double priorityScore)
    // ------------------------------------------------------------
    // Requirements: every possible double value must map to EXACTLY
    // one label — no gaps. Use this threshold scheme:
    //   >= 8.0           -> "DO NOW"
    //   >= 6.0 (but < 8)  -> "THIS SPRINT"
    //   >= 3.0 (but < 6)  -> "BACKLOG"
    //   anything else     -> "MONITOR"
    //
    // Give it a Javadoc comment with @param and @return.
    //
    // public static String getPriorityLabel(double priorityScore) {
    //     // your code here
    // }

}

/*
 * ============================================================
 * EXPECTED OUTPUT ONCE ALL TODOS ARE COMPLETE
 * ============================================================
 * === Step 1: The Problem (monolithic, repeated logic) ===
 * System 1 priority (no validation): 5.5
 * System 2 priority (no validation): 6.5
 *
 * === Step 2: The Fix (decomposed, validated methods) ===
 * Two-factor priority: 5.5 (BACKLOG)
 * Three-factor priority: 6.7 (THIS SPRINT)
 *
 * === Step 3: Validation Actually Works ===
 * Correctly rejected invalid input: Urgency must be between 0 and 10, got: -5.0
 * ============================================================
 */
