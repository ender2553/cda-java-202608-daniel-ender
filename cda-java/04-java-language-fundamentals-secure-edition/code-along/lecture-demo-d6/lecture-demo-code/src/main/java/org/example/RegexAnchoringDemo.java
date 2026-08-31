package org.example;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexAnchoringDemo {

    public static void main(String[] args) {

        System.out.println("--- Unanchored regex: a dangerous false positive ---");
        demoUnanchoredRegex();

        System.out.println();
        System.out.println("--- Anchored regex: correctly rejects the same input ---");
        demoAnchoredRegex();

        System.out.println();
        System.out.println("--- matches() is always implicitly anchored ---");
        demoMatchesIsAnchored();

        System.out.println();
        System.out.println("--- The sanitization trap: cleaning can CREATE the attack ---");
        demoSanitizationTrap();
    }

    private static void demoUnanchoredRegex() {
        String pattern = "CVE-\\d{4}-\\d+";
        String maliciousInput = "IGNORED-CVE-2024-1234-INJECTED";

        Matcher m = Pattern.compile(pattern).matcher(maliciousInput);
        boolean isValid = m.find();
        System.out.println("find() on padded input: " + isValid + " (WRONG - should be rejected)");
    }

    private static void demoAnchoredRegex() {
        String anchoredPattern = "^CVE-\\d{4}-\\d+$";
        String maliciousInput = "IGNORED-CVE-2024-1234-INJECTED";

        Matcher m = Pattern.compile(anchoredPattern).matcher(maliciousInput);
        boolean isValid = m.find();
        System.out.println("find() on padded input, pattern now anchored: " + isValid + " (correct)");
    }

    private static void demoMatchesIsAnchored() {
        String pattern = "CVE-\\d{4}-\\d+"; // no ^ or $ needed
        String maliciousInput = "IGNORED-CVE-2024-1234-INJECTED";
        String validInput = "CVE-2024-1234";

        System.out.println("maliciousInput.matches(pattern): " + maliciousInput.matches(pattern) + " (correct)");
        System.out.println("validInput.matches(pattern): " + validInput.matches(pattern) + " (correct)");
    }

    private static void demoSanitizationTrap() {
        String input = "<scr<script>ipt>";
        String sanitized = input.replace("<script>", "");
        System.out.println("Original:  " + input);
        System.out.println("\"Cleaned\": " + sanitized);
        System.out.println("Still contains <script>? " + sanitized.contains("<script>"));
    }

}


