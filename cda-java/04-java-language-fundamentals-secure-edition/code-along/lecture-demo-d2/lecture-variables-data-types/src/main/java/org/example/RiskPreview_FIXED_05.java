package org.example;

public class RiskPreview_FIXED_05 {

    public static void main(String[] args) {

        // FIX OPTION 1: declare the variables as double from the start.
        // This is the preferred fix — it prevents the bug at the source
        // rather than patching it at the point of use. likelihood = 7; impact = 8 riskScore = (likelihood + impact) / 2;

        //System.out.println("Fix 1 (double variables): " + riskScore);

        // FIX OPTION 2: if you're stuck with int variables (e.g., they
        // come from elsewhere in a larger program), cast at the point of
        // division so the math itself happens in double precision.

        //System.out.println("Fix 2 (cast at division): " + riskScoreCast);

        // COMMON MISTAKE: casting the WRONG thing. This still produces 7,
        // because the division happens as int math BEFORE the result
        // gets converted to double — the cast is applied too late.
        //Show common mistake

    }

}


