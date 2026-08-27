package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 4
 * Risk Score Calculator, Refactored
 * ============================================================
 *
 * SCENARIO:
 *   Your Day 2 CVSS calculator worked, but it was one long block of
 *   arithmetic in main(). Today you'll refactor it into well-named,
 *   independently validated methods — the same transformation from
 *   today's lecture (BeforeMethods -> AfterMethods), applied to your
 *   own Day 2 work.
 *
 * REQUIREMENTS (this is what's graded — see rubric at the bottom):
 *   1. Extract each calculation into its own well-named method.
 *   2. Each method validates its own inputs at the boundary.
 *   3. Every method has a Javadoc comment with @param, @return, and
 *      @throws (where applicable) — see the example shapes below.
 *   4. main() should mostly just call methods and print results —
 *      not contain the arithmetic itself.
 *   5. Use appropriate parameter and return types throughout.
 *   6. Keep each method focused on ONE clear task.
 *
 * RULES:
 *   - This is INDIVIDUAL work. Do not copy from a classmate.
 *   - You may bring your own Day 2 CvssCalculator.java logic in as a
 *     starting point for what each method should calculate.
 *   - Test with both valid and invalid inputs (e.g., a score of -5 or
 *     15 to confirm your validation actually triggers).
 *
 * SUBMISSION:
 *   Save this file as RiskCalculatorRefactored.java and submit it along
 *   with output from at least 2 test runs, including one that
 *   deliberately triggers a validation error.  Text file or .docx file.
 * ============================================================
 */

public class RiskCalculatorRefactored {

    // ------------------------------------------------------------
    // TODO 1: Write a method calculateLikelihood() that returns a
    // double representing a likelihood metric. You decide the
    // parameters — for example, it could take a few sub-factors and
    // combine them, or simply take one value and validate/return it.
    // At minimum, it should validate that its inputs are in a
    // reasonable range (e.g., 0-10) and throw an
    // IllegalArgumentException if not, just like averageRisk() did in
    // today's lecture demo. Give it a Javadoc comment with @param,
    // @return, and @throws.
    //
    // Example shape:
    //   /**
    //    * Validates and returns a likelihood value.
    //    *
    //    * @param rawValue the raw likelihood input, expected 0-10
    //    * @return the validated likelihood value
    //    * @throws IllegalArgumentException if rawValue is outside 0-10
    //    */
    //   public static double calculateLikelihood(double rawValue) {
    //       if (rawValue < 0.0 || rawValue > 10.0) {
    //           throw new IllegalArgumentException("Likelihood must be 0-10");
    //       }
    //       return rawValue;
    //   }
    // ------------------------------------------------------------

    /**
     * Validates and returns a likelihood value.
     *
     * @param rawValue the raw likelihood input, expected 0-10
     * @return the validated likelihood value
     * @throws IllegalArgumentException if rawValue is outside 0-10
     */
    public static double calculateLikelihood(double rawValue) {
        if (rawValue < 0.0 || rawValue > 10.0) {
            throw new IllegalArgumentException("Likelihood must be 0-10");
        }

        return rawValue;
    }


    // ------------------------------------------------------------
    // TODO 2: Write a method calculateImpact() following the same
    // pattern as TODO 1, but representing the impact metric. Don't
    // forget its Javadoc comment too.
    // ------------------------------------------------------------

    /**
     * Validates and returns an impact value.
     *
     * @param rawValue the raw impact input, expected 0-10
     * @return the validated impact value
     * @throws IllegalArgumentException if rawValue is outside 0-10
     */
    public static double calculateImpact(double rawValue) {
        if (rawValue < 0.0 || rawValue > 10.0) {
            throw new IllegalArgumentException("Impact must be 0-10");
        }

        return rawValue;
    }


    // ------------------------------------------------------------
    // TODO 3: Write a method calculateRiskScore(double likelihood,
    // double impact) that combines the two values into a single risk
    // score using whatever formula you designed back on Day 1 (or a
    // new one — your choice). This method should NOT re-validate
    // likelihood and impact — trust that calculateLikelihood() and
    // calculateImpact() already did that job. (This is intentional:
    // it demonstrates that each method has ONE clear responsibility.)
    // Its Javadoc needs @param and @return, but NOT @throws — this
    // method doesn't validate anything, so it can't throw.
    // ------------------------------------------------------------

    /**
     * Calculates the overall risk score using likelihood and impact.
     *
     * @param likelihood the validated likelihood value
     * @param impact the validated impact value
     * @return the calculated risk score
     */
    public static double calculateRiskScore(double likelihood, double impact) {
        return likelihood * impact;
    }

    // ------------------------------------------------------------
    // TODO 4: Write a method getSeverityLabel(double riskScore) that
    // returns a String ("LOW", "MEDIUM", "HIGH", or "CRITICAL") based
    // on your own thresholds. Make sure every possible score value
    // results in SOME label — no gaps, per Day 2's fail-secure lesson.
    // Give it a Javadoc comment with @param and @return.
    // ------------------------------------------------------------

    /**
     * Assigns the severity label based on the calculated risk score.
     *
     * @param riskScore the calculated risk score
     * @return LOW, MEDIUM, HIGH, or CRITICAL based on the risk score
     */
    public static String getSeverityLabel(double riskScore) {
        if (riskScore < 25.0) {
            return "LOW";
        } else if (riskScore < 50.0) {
            return "MEDIUM";
        } else if (riskScore < 75.0) {
            return "HIGH";
        } else {
            return "CRITICAL";
        }
    }


    public static void main(String[] args) {


        // ------------------------------------------------------------
        // TODO 5: Call your methods in sequence to calculate and print
        // a full result, similar to your Day 1 calculator's behavior.
        // main() should read like a clear, short sequence of steps —
        // NOT contain any of the actual arithmetic itself.
        //
        // Example shape:
        //   double likelihood = calculateLikelihood(7.0);
        //   double impact = calculateImpact(8.0);
        //   double riskScore = calculateRiskScore(likelihood, impact);
        //   String severity = getSeverityLabel(riskScore);
        //   System.out.printf("Risk Score: %.1f (%s)%n", riskScore, severity);
        // ------------------------------------------------------------

        double likelihood = calculateLikelihood(4.0);
        double impact = calculateImpact(6.0);
        double riskScore = calculateRiskScore(likelihood, impact);
        String severity = getSeverityLabel(riskScore);

        System.out.printf("Risk Score: %.1f (%s)%n", riskScore, severity);


        // ------------------------------------------------------------
        // TODO 6: Add a second call that deliberately passes an
        // out-of-range value (e.g., -5.0) to one of your methods,
        // wrapped in a try/catch block, to demonstrate your validation
        // actually works. try/catch is Day 7 material, but the pattern
        // below is enough to use today:
        //
        //   try {
        //       calculateLikelihood(-5.0);
        //   } catch (IllegalArgumentException e) {
        //       System.out.println("Correctly rejected: " + e.getMessage());
        //   }
        // ------------------------------------------------------------

        try {
            calculateLikelihood(-5.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected: " + e.getMessage());

        }

    }
}







/*
 * ============================================================
 * GRADING RUBRIC (100 points total)
 * ============================================================
 *   20 pts — Decomposition quality: logic is split into sensible,
 *            focused methods (not one giant method renamed)
 *   15 pts — Method signature design: clear, appropriate parameter and
 *            return types for each method
 *   20 pts — Boundary validation: each input-facing method validates
 *            its own inputs and throws on invalid data
 *   15 pts — Javadoc: every method has an accurate Javadoc block comment
 *            with @param for each parameter, @return (if not void),
 *            and @throws for any exception it can throw
 *   15 pts — Correct behavior: the refactored calculator produces
 *            sensible, consistent results across valid inputs
 *   15 pts — Code compiles and runs without errors, including the
 *            deliberate validation-error test case
 * ============================================================
 */
