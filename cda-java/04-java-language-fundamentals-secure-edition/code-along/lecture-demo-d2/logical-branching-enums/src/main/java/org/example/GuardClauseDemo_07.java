package org.example;



public class GuardClauseDemo_07 {

    public static void main(String[] args) {

        processScore(-3.0); // invalid input example
        System.out.println();
        processScore(6.5);  // valid input example
    }

    private static void processScore(double cvssScore) {
        System.out.println("Processing score: " + cvssScore);

        // GUARD CLAUSE: check for the invalid condition FIRST and exit
        // immediately. Everything below this point can now safely assume
        // cvssScore is a valid, in-range value.
        if (cvssScore < 0.0 || cvssScore > 10.0) {
            System.out.println("ERROR: CVSS score out of valid range (0-10). Escalating for manual review.");
            return; // exit the method immediately — fail-secure
        }

        // "Happy path" logic — flat, readable, and doesn't need to
        // re-check validity because the guard clause already handled it.
        System.out.println("Valid score confirmed: " + cvssScore);
    }



}


