/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 1/2
 * CVSS Score Calculator
 * ============================================================
 *
 * SCENARIO:
 *   Your manager wants a command-line tool that replaces the shared
 *   spreadsheet analysts currently use to score vulnerabilities by hand.
 *   You will build a calculator that reads CVSS-style metric inputs from
 *   the analyst, computes a combined score, and prints a severity label.
 *
 * REQUIREMENTS (this is what's graded — see rubric at the bottom):
 *   1. Prompt for AT LEAST 3 numeric metric inputs via Scanner.
 *   2. Use double for any value that can be a decimal.
 *   3. Calculate a combined score using arithmetic operators.
 *   4. Print the score with formatted output (printf).
 *   5. Print a severity label using simple threshold logic (if/else is
 *      fine even though we haven't formally covered it yet — a chain of
 *      if statements works for this lab; full branching is Day 2's topic).
 *
 * RULES:
 *   - This is INDIVIDUAL work. Do not copy from a classmate.
 *   - You may use your Day 1 code-along files as reference.
 *   - Test your program with several different inputs before submitting,
 *     including an edge case (e.g., a score of exactly 0 or exactly 10).
 *
 * SUBMISSION:
 *   Save this file as CvssCalculator.java and submit it along with a
 *   screenshot or text output showing at least 2 successful test runs.
 * ============================================================
 */

import java.util.Scanner;

public class CvssCalculator {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        // ------------------------------------------------------------
        // TODO 1: Declare a variable to hold the "Attack Vector" metric
        // as a double. Prompt the analyst to enter a value (suggest a
        // scale of your choosing, e.g., 0.0-2.0), and read it with
        // input.nextDouble().
        //
        // Example prompt text: "Enter Attack Vector score (0.0-2.0): "
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 2: Declare a variable to hold the "Attack Complexity"
        // metric as a double. Prompt and read it the same way as TODO 1.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 3: Declare a variable to hold the "Impact" metric as a
        // double. Prompt and read it the same way as TODO 1.
        //
        // (This satisfies the "at least 3 numeric inputs" requirement.
        // Feel free to add a 4th or 5th metric for extra realism.)
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 4: Calculate a combined score using the three (or more)
        // values above. You get to design the formula — it does not
        // need to match the real CVSS formula. A simple, defensible
        // approach is fine, for example:
        //
        //   double combinedScore = attackVector + attackComplexity + impact;
        //
        // Think about: should any value be weighted more than another?
        // Store the result in a double variable.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 5: Print the combined score using System.out.printf with
        // at least 1 decimal place of precision. Review Day 1's CLI
        // formatting demo (07_CliArgsAndFormatting.java) if needed.
        //
        // Example: System.out.printf("Combined Score: %.1f%n", combinedScore);
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 6: Using a chain of if / else if / else statements,
        // print a severity label based on your combinedScore. Choose
        // your own thresholds, but make sure every possible score value
        // (including negative or very high, if someone mistypes) results
        // in SOME label being printed — don't leave a gap.
        //
        // Example structure (fill in your own thresholds):
        //   if (combinedScore >= ???) {
        //       System.out.println("CRITICAL");
        //   } else if (combinedScore >= ???) {
        //       System.out.println("HIGH");
        //   } else if (combinedScore >= ???) {
        //       System.out.println("MEDIUM");
        //   } else {
        //       System.out.println("LOW");
        //   }
        // ------------------------------------------------------------


        input.close();
    }

}

/*
 * ============================================================
 * GRADING RUBRIC (100 points total)
 * ============================================================
 *   20 pts — Correct type selection: all score-related variables use
 *            double, not int
 *   25 pts — Accurate, working arithmetic: combined score calculates
 *            without errors and reflects all input values
 *   20 pts — Clean Scanner-based input handling: at least 3 numeric
 *            prompts, each clearly labeled
 *   20 pts — Properly formatted printf output: score is printed with
 *            controlled decimal precision, not a raw double
 *   15 pts — Code compiles and runs without errors, and every input
 *            value results in a printed severity label (no silent gaps)
 *
 * Well-reasoned formula design is accepted even if it differs from
 * other students' approaches — you are not being graded against a
 * single "correct" formula, only for sound type usage, working
 * arithmetic, and clean I/O.
 * ============================================================
 */
