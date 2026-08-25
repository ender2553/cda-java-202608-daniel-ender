package org.example;


public class SwitchDemo_03 {

    public static void main(String[] args) {

        System.out.println("=== Modern arrow syntax ===");
        modernSwitch("HIGH");

        System.out.println();
        System.out.println("=== Old syntax WITH a (bug) ===");
        brokenOldSwitch("MEDIUM");
    }

    private static void modernSwitch(String severity) {
        switch (severity) {
            case "CRITICAL" -> System.out.println("Patch within 24 hours");
            case "HIGH"     -> System.out.println("Patch within 7 days");
            case "MEDIUM"   -> System.out.println("Patch within 30 days");
            default         -> System.out.println("Unrecognized severity - escalate for review");
        }
    }

    // DELIBERATELY BUGGY: What's the issue?.
    private static void brokenOldSwitch(String severity) {
        switch (severity) {
            case "CRITICAL":
                System.out.println("Patch within 24 hours");
                break;
            case "MEDIUM":
                System.out.println("Patch within 30 days");
            case "LOW":
                System.out.println("Patch within 90 days");
                break;
            default:
                System.out.println("Unrecognized severity - escalate for review");
                break;
        }
    }

}


