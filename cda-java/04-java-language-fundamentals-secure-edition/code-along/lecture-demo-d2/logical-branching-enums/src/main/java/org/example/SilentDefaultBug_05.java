package org.example;


public class SilentDefaultBug_05 {

    public static void main(String[] args) {

        String severity = "hihg";

        switch (severity) {
            case "CRITICAL":
                System.out.println("Patch in 24h");
                break;
            case "HIGH":
                System.out.println("Patch in 7 days");
                break;
            default:
                System.out.println("No action needed");
                break;
        }
    }

}


