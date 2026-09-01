package org.example;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * A small console app for exercising your RiskRegister implementation
 * by hand. This file is already complete — you don't need to modify
 * it, but running it is a good way to sanity-check your work before
 * (and while) your test suite is still being written.
 *
 * This will not compile until RiskRegister.java's TODOs are filled in.
 */
public class RiskRegisterApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RiskRegister register = new RiskRegister();
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().strip();

            switch (choice) {
                case "1" -> addFindingInteractively(scanner, register);
                case "2" -> printFindings(register);
                case "3" -> printUniqueCveIds(register);
                case "4" -> saveRegister(scanner, register);
                case "5" -> register = loadRegister(scanner, register);
                case "6" -> importLegacyScan(scanner, register);
                case "0" -> {
                    System.out.println("Goodbye.");
                    running = false;
                }
                default -> System.out.println("Not a valid option. Try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== RISK REGISTER CONSOLE ===");
        System.out.println("1) Add a finding");
        System.out.println("2) View all findings");
        System.out.println("3) View unique CVE IDs");
        System.out.println("4) Save register to file");
        System.out.println("5) Load register from file");
        System.out.println("6) Import legacy scan results");
        System.out.println("0) Exit");
        System.out.print("Choose an option: ");
    }

    private static void addFindingInteractively(Scanner scanner, RiskRegister register) {
        System.out.print("CVE ID (e.g. CVE-2024-1234): ");
        String cveId = scanner.nextLine().strip();

        System.out.print("Component (e.g. openssl): ");
        String component = scanner.nextLine().strip();

        System.out.print("Version (e.g. 3.0.1): ");
        String version = scanner.nextLine().strip();

        Severity severity = null;
        while (severity == null) {
            System.out.print("Severity [CRITICAL, HIGH, MEDIUM, LOW]: ");
            try {
                severity = Severity.valueOf(scanner.nextLine().strip().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Not a recognized severity. Try again.");
            }
        }

        BigDecimal cost;
        while (true) {
            System.out.print("Estimated remediation cost (e.g. 150.00): ");
            try {
                cost = new BigDecimal(scanner.nextLine().strip());
                break;
            } catch (NumberFormatException e) {
                System.out.println("That doesn't look like a number. Try again.");
            }
        }

        LocalDate discoveredDate;
        while (true) {
            System.out.print("Discovered date (YYYY-MM-DD): ");
            try {
                discoveredDate = LocalDate.parse(scanner.nextLine().strip());
                break;
            } catch (DateTimeParseException e) {
                System.out.println("That doesn't look like a date. Try again.");
            }
        }

        try {
            Vulnerability finding = new Vulnerability(cveId, component, version, severity, cost, discoveredDate);
            register.addFinding(finding);
            System.out.println("Added: " + finding);
        } catch (IllegalArgumentException | DuplicateFindingException e) {
            System.out.println("Could not add that finding: " + e.getMessage());
        }
    }

    private static void printFindings(RiskRegister register) {
        System.out.println("--- Findings (" + register.getFindings().size() + ") ---");
        for (Vulnerability finding : register.getFindings()) {
            System.out.println("  " + finding);
        }
    }

    private static void printUniqueCveIds(RiskRegister register) {
        System.out.println("--- Unique CVE IDs (" + register.getUniqueCveIds().size() + ") ---");
        for (String cveId : register.getUniqueCveIds()) {
            System.out.println("  " + cveId);
        }
    }

    private static void saveRegister(Scanner scanner, RiskRegister register) {
        System.out.print("Save to filename: ");
        String filename = scanner.nextLine().strip();
        try {
            register.saveToFile(filename);
            System.out.println("Saved to " + filename);
        } catch (IOException e) {
            System.out.println("Could not save the register right now. Please try again.");
        }
    }

    private static RiskRegister loadRegister(Scanner scanner, RiskRegister currentRegister) {
        System.out.print("Load from filename: ");
        String filename = scanner.nextLine().strip();
        try {
            RiskRegister loaded = RiskRegister.loadFromFile(filename);
            System.out.println("Loaded " + loaded.getFindings().size() + " finding(s) from " + filename);
            return loaded;
        } catch (IOException e) {
            System.out.println("No register file found named \"" + filename + "\".");
        } catch (InvalidRegisterDataException e) {
            System.out.println("That register file is corrupted and could not be loaded: " + e.getMessage());
        }
        return currentRegister;
    }

    private static void importLegacyScan(Scanner scanner, RiskRegister register) {
        System.out.print("Legacy scan filename: ");
        String filename = scanner.nextLine().strip();
        try {
            register.importLegacyScanResults(filename);
            System.out.println("Legacy scan results imported from " + filename);
        } catch (IOException e) {
            System.out.println("No legacy scan file found named \"" + filename + "\".");
        } catch (InvalidRegisterDataException e) {
            System.out.println("Could not import that legacy scan file: " + e.getMessage());
        }
    }
}
