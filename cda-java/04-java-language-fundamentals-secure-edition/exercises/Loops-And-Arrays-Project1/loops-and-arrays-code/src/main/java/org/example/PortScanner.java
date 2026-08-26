package org.example;

/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 3
 * Port/Service Scanner Simulator
 * ============================================================
 *
 * SCENARIO:
 *   Your manager wants a tool that checks a whole list of open ports on
 *   a target against a list of known-vulnerable ports/services — like a
 *   miniature version of a real scanning tool (e.g., Nessus). You will
 *   loop through both lists, report any matches, and be careful never
 *   to access an array index that doesn't exist.
 *
 * REQUIREMENTS (this is what's graded — see rubric at the bottom):
 *   1. Store the target's open ports/services in one array.
 *   2. Store known-vulnerable ports/services in a second array.
 *   3. Loop through both to find and report matches.
 *   4. Use correct loop boundaries — no off-by-one errors.
 *   5. Print a clear findings report, including a "no issues found" case.
 *
 * RULES:
 *   - This is INDIVIDUAL work. Do not copy from a classmate.
 *   - You may use your Day 3 code-along files as reference.
 *   - Test with a target that HAS a vulnerable match, one that does
 *     NOT, and pay close attention to your array boundaries.
 *
 * SUBMISSION:
 *   Save this file as PortScanner.java and submit it along with output
 *   showing at least 2 test runs (one with a match, one without).
 * ============================================================
 */

public class PortScanner {

    public static void main(String[] args) {

        // ------------------------------------------------------------
        // TODO 1: Declare an int array representing the OPEN PORTS found
        // on a simulated target (e.g., {22, 80, 443, 3389, 8080}).
        // Feel free to use different values than this example.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 2: Declare a second int array representing KNOWN-
        // VULNERABLE ports (e.g., a port that's outdated or misconfigured
        // if left open, like {21, 23, 3389}). At least one value in this
        // array should also appear in your Day 3 TODO 1 array, so you
        // can test that a match is correctly detected.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 3: Using nested loops (a loop inside a loop), compare
        // every value in your open-ports array against every value in
        // your known-vulnerable array. When you find a match, print a
        // finding, e.g.:
        //     "FINDING: Port 3389 is open and matches a known-vulnerable port!"
	    //
	    //
        // Structure hint:
        //   for (int i = 0; i < openPorts.length; i++) {
        //       for (int j = 0; j < knownVulnerablePorts.length; j++) {
        //           // compare openPorts[i] to knownVulnerablePorts[j]
        //       }
        //   }
        //
        // IMPORTANT: double-check your loop conditions use < and NOT <=
        // against .length. This is the exact bug from today's lecture.
	    //
	    //
	    // NOTE: THERE IS A BETTER WAY TO HANDLE THIS OTHER THAN NESTED FOR LOOPS - WILL COVER THIS LATER.
        // ------------------------------------------------------------


        // ------------------------------------------------------------
        // TODO 4: Track whether ANY finding was reported (e.g., with a
        // boolean flag you set to true inside TODO 3's matching logic).
        // After both loops finish, if NO findings were reported, print
        // a clear "No known-vulnerable ports found" message. Don't leave
        // the analyst with no output at all if nothing matched.
        // ------------------------------------------------------------


    }

}

/*
 * ============================================================
 * GRADING RUBRIC (100 points total)
 * ============================================================
 *   20 pts — Loop correctness: both loops use proper boundaries
 *            (< against .length, not <=)
 *   30 pts — Accurate matching logic: every true match between the two
 *            arrays is correctly identified and reported
 *   25 pts — No ArrayIndexOutOfBoundsException on any valid input,
 *            including arrays of different lengths
 *   15 pts — Clean, readable findings report output, including a
 *            correct "no issues found" case when there are no matches
 *   10 pts — Code compiles and runs without errors
 *
 * Different array sizes, values, and even different comparison
 * strategies (nested loops vs. other approaches) are all acceptable —
 * you are graded on correct, working, bounds-safe logic, not on
 * matching one specific implementation.
 * ============================================================
 */
