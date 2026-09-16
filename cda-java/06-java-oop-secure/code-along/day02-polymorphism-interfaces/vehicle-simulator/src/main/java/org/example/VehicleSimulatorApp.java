package org.example;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class VehicleSimulatorApp {

    private static List<Transportable> buildFleet() {
        List<Transportable> fleet = new ArrayList<>();
        fleet.add(new Car("Toyota", "Corolla", 2025, new Engine("gasoline"), 12.0));
        fleet.add(new Motorcycle("Honda", "Rebel 300", 2023, new Engine("gasoline"), 3.0));
        fleet.add(new Truck("Ford", "F-150", 2026, new Engine("diesel"), 25.0, 1.5));
        fleet.add(new Plane("Boeing", "737", 2019, new Engine("jet fuel"), 6000.0, 36000));
        fleet.add(new Boat("Beneteau", "Oceanis 30", 2020, new Engine("gasoline"), 40.0, "monohull"));
        fleet.add(new Train("Amtrak", "Acela", 2018, new Engine("electric"), 500.0, 8));
        fleet.add(new Spaceship("USCSS Nostromo", 100.0));
        return fleet;
    }

    public static void main(String[] args) {
        List<Transportable> fleet = buildFleet();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Vehicle Simulator: Day 2 (Interfaces & Polymorphism) ===");
        printFleet(fleet);

        boolean running = true;
        while (running) {
            printMenu();
            String choice;
            try {
                choice = scanner.nextLine().trim();
            } catch (NoSuchElementException e) {
                // secure coding: fail gracefully on closed/exhausted input
                // instead of letting a raw stack trace reach the user.
                System.out.println("\nInput closed. Exiting.");
                break;
            }

            switch (choice) {
                case "1" -> printFleet(fleet);
                case "2" -> moveAll(fleet, scanner);
                case "3" -> runAuditLog(fleet, scanner);
                case "4" -> addSpaceship(fleet, scanner);
                case "0" -> running = false;
                default -> System.out.println("Please enter a number from the menu.");
            }
        }
        System.out.println("Simulator closed.");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1) List fleet");
        System.out.println("2) Move every fleet member by N units (polymorphism)");
        System.out.println("3) Attempt access to a restricted (AuditLoggable) asset");
        System.out.println("4) Add a new Spaceship to the fleet");
        System.out.println("0) Exit");
        System.out.print("Choice: ");
    }

    private static void printFleet(List<Transportable> fleet) {
        System.out.println();
        System.out.println("--- Current Fleet (" + fleet.size() + ") ---");
        for (Transportable t : fleet) {
            // Polymorphism: we only know each item is "a Transportable"
            // here, yet toString() below still calls each object's OWN
            // real class's toString() - Car's, Spaceship's, whichever it
            // actually is underneath.
            System.out.println("  - " + t.getName() + "  [" + t + "]");

            // Every Vehicle subclass can additionally honk() - but
            // honk() isn't part of Transportable (a Spaceship doesn't
            // honk). instanceof pattern matching lets us safely reach
            // subclass-specific behavior WITHOUT risking a
            // ClassCastException on the items that don't support it.
            if (t instanceof Vehicle v) {
                System.out.println("      honk(): " + v.honk());
            }
        }
    }

    private static void moveAll(List<Transportable> fleet, Scanner scanner) {
        int units = readPositiveInt(scanner, "How many units should every fleet member attempt to move? ");
        System.out.println();
        // THE payoff of Transportable: one loop, seven different kinds
        // of object, zero if-else chains asking "is this a Car? a
        // Spaceship?". Each one already knows how to move() itself.
        for (Transportable t : fleet) {
            System.out.println(t.move(units));
        }
    }

    private static void runAuditLog(List<Transportable> fleet, Scanner scanner) {
        List<AuditLoggable> restricted = new ArrayList<>();
        for (Transportable t : fleet) {
            if (t instanceof AuditLoggable auditable) {
                restricted.add(auditable);
            }
        }
        if (restricted.isEmpty()) {
            System.out.println("No AuditLoggable assets in the fleet yet.");
            return;
        }
        System.out.println();
        System.out.println("Restricted assets requiring an operator ID:");
        for (int i = 0; i < restricted.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + ((Transportable) restricted.get(i)).getName());
        }
        int index = readIndexInRange(scanner, "Select an asset: ", restricted.size());
        System.out.print("Operator ID: ");
        String operatorId = scanner.nextLine().trim();
        if (operatorId.isEmpty()) {
            // secure coding: never silently accept a blank identity for
            // an audited action.
            System.out.println("Operator ID cannot be blank. Access attempt rejected and NOT logged.");
            return;
        }
        restricted.get(index - 1).logAccessAttempt(operatorId);
    }

    private static void addSpaceship(List<Transportable> fleet, Scanner scanner) {
        System.out.print("New spaceship name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be blank. Spaceship not added.");
            return;
        }
        double power = readPositiveDouble(scanner, "Starting power cell level (0-100): ");
        try {
            fleet.add(new Spaceship(name, power));
            System.out.println(name + " added to the fleet.");
        } catch (IllegalArgumentException e) {
            // secure coding: validation lives in the constructor, so
            // this menu handler just has to report the rejection
            // instead of re-checking the same rules itself.
            System.out.println("Could not add spaceship: " + e.getMessage());
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to re-prompt
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }

    private static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value >= 0 && value <= 100) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to re-prompt
            }
            System.out.println("Please enter a number between 0 and 100.");
        }
    }

    private static int readIndexInRange(Scanner scanner, String prompt, int size) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= 1 && value <= size) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to re-prompt
            }
            System.out.println("Please enter a number between 1 and " + size + ".");
        }
    }
}
