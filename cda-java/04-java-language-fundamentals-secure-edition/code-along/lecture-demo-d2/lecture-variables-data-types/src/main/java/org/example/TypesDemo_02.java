package org.example;


public class TypesDemo_02 {

    public static void main(String[] args) {

        // int: whole numbers only — good for counts, ports, indexes


        // double: decimal numbers — required for anything that can have
        // a fractional value, like a CVSS score


        // boolean: true/false — good for yes/no security facts


        // char: a single character — e.g., a one-letter severity shorthand


        // long: whole numbers larger than int can hold — rarely needed at
        // this level, but exists for things like large timestamps or IDs
         // note the trailing 'L'

        // String (reference type preview): text. Notice the capital S —
        // that's your first hint that String is not a primitive. We will
        // explain exactly what that means on Day 5.


        System.out.println("Asset: " + assetName);
        System.out.println("Open ports: " + openPortCount);
        System.out.println("CVSS score: " + cvssScore);
        System.out.println("Patched? " + isPatched);
        System.out.println("Exploitable? " + isExploitable);
        System.out.println("Severity code: " + severityCode);
        System.out.println("Scan timestamp (ms): " + scanTimestampMillis);
    }

}


