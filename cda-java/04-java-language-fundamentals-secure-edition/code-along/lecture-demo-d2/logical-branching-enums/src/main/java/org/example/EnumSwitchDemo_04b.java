package org.example;


public class EnumSwitchDemo_04b {

    public static void main(String[] args) {

        Severity current = Severity.HIGH;

        switch (current) {
            case CRITICAL -> System.out.println("Patch within 24 hours");
            case HIGH     -> System.out.println("Patch within 7 days");
            case MEDIUM   -> System.out.println("Patch within 30 days");
            case LOW      -> System.out.println("Patch within 90 days");
        }

        // Uncomment the line below — Discuss

        // Severity typo = Severity.CRITCAL;
    }

}

