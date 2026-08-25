package org.example;



public class BooleanOperatorsDemo_02 {

    public static void main(String[] args) {

        boolean isExploitable = true;
        boolean isInternetFacing = true;
        boolean isPatched = false;
        boolean isActivelyExploited = false;
        boolean isCritical = true;

        // AND (&&): both sides must be true
        boolean urgent = isExploitable && isInternetFacing;
        System.out.println("Urgent (exploitable AND internet-facing): " + urgent);

        // OR (||): at least one side must be true
        boolean flagForReview = isCritical || isActivelyExploited;
        System.out.println("Flag for review (critical OR actively exploited): " + flagForReview);

        // NOT (!): flips a boolean
        boolean needsAttention = !isPatched;
        System.out.println("Needs attention (NOT patched): " + needsAttention);

        // Short-circuit evaluation demo:
        // Because isPatched is false, Java never even calls
        // checkDatabaseForCVE() — the right side of && is skipped entirely
        // once the left side is known to be false.
        System.out.println("--- Short-circuit demo ---");
        boolean result = isPatched && checkDatabaseForCVE();
        System.out.println("Result: " + result);
    }

    // This method prints a message so we can SEE whether it actually runs.
    private static boolean checkDatabaseForCVE() {
        System.out.println(">>> checkDatabaseForCVE() actually ran! <<<");
        return true;
    }

}


