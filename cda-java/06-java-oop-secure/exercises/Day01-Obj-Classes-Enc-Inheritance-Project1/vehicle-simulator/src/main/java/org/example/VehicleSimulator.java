package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;


/**
 * VehicleSimulator.java
 * ===================================================================================
 * DAY 1 — Java-OOP-Secure - Classes & Objects, Encapsulation, Inheritance, collections
 * ------------------------------------------------------------------------------------
 */
public class VehicleSimulator {

    private final List<Vehicle> fleet;
    private final Queue<Vehicle> serviceLine;
    private final Scanner scanner;

    public VehicleSimulator(List<Vehicle> fleet, Scanner scanner) {
        this.fleet = fleet;
        this.serviceLine = new LinkedList<>();
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        List<Vehicle> startingFleet = createStarterFleet();
        Scanner scanner = new Scanner(System.in);
        VehicleSimulator simulator = new VehicleSimulator(startingFleet, scanner);
        simulator.run();
    }

    // Seeds the fleet from existing data - the same
    // `new ArrayList<>(List.of(...))` pattern taught in ListsDemo.java.
    private static List<Vehicle> createStarterFleet() {
        return new ArrayList<>(List.of(
                new Car("Toyota", "Corolla", 2021, new Engine("gasoline"), 8),
                new Motorcycle("Honda", "Rebel 300", 2022, new Engine("gasoline"), 3),
                new Truck("Ford", "F-150", 2023, new Engine("diesel"), 20, 1.5)
        ));
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> viewFleet();
                case 2 -> addVehicle();
                case 3 -> driveVehicle();
                case 4 -> refuelVehicle();
                case 5 -> honkVehicle();
                case 6 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }

                case 7 -> viewMakes();
                case 8 -> viewInventoryByMake();
                case 9 -> sendToServiceLine();
                case 10 -> serviceNextVehicle();
                default -> System.out.println("Not a valid option - try again.");
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=== Vehicle Simulator ===");
        System.out.println("1. View fleet");
        System.out.println("2. Add a vehicle");
        System.out.println("3. Drive a vehicle");
        System.out.println("4. Refuel a vehicle");
        System.out.println("5. Honk a vehicle");
        System.out.println("6. Exit");
        System.out.println("7. View makes in the fleet");
        System.out.println("8. View inventory by make");
        System.out.println("9. Send a vehicle to the service line");
        System.out.println("10. Service the next vehicle in line");
    }

    private void viewFleet() {
        if (fleet.isEmpty()) {
            System.out.println("The fleet is empty.");
            return;
        }
        System.out.println("Current fleet (" + fleet.size() + " vehicles):");
        for (int i = 0; i < fleet.size(); i++) {
            System.out.println(" [" + i + "] " + fleet.get(i));
        }
    }

    private void viewMakes() {
        Set<String> makes = new HashSet<>();

        for (Vehicle vehicle : fleet) {
            makes.add(vehicle.getMake());
        }

        System.out.println("Unique makes in the fleet: " + makes);
    }

    private void viewInventoryByMake() {
        Map<String, Integer> inventory = new HashMap<>();

        for (Vehicle vehicle : fleet) {
            String make = vehicle.getMake();
            inventory.put(make, inventory.getOrDefault(make, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " vehicle(s)");
        }
    }

    private void sendToServiceLine() {
        Vehicle vehicle = selectVehicle();
        if (vehicle == null) return;

        serviceLine.offer(vehicle);
        System.out.println("Sent to service line: " + vehicle.describe());
    }

    private void serviceNextVehicle() {
        Vehicle vehicle = serviceLine.poll();

        if (vehicle == null) {
            System.out.println("The service line is empty.");
            return;
        }

        System.out.println("Serviced: " + vehicle.describe());
    }

    private void addVehicle() {
        // FUTURE ENHANCEMENT (interfaces & polymorphism day): this menu
        // will grow to include Plane, Boat, Spaceship, Train, etc. via a
        // shared interface instead of three hardcoded cases.
        System.out.println("Vehicle type: 1) Car  2) Motorcycle  3) Truck  4) Bus");
        int type = readInt("Choose a type: ");

        String make = readLine("Make: ");
        String model = readLine("Model: ");
        int year = readInt("Year: ");
        String engineType = readLine("Engine type (gasoline/diesel/electric): ");
        double startingFuel = readDouble("Starting fuel (gallons): ");

        Vehicle vehicle;
        switch (type) {
            case 1 -> vehicle = new Car(make, model, year, new Engine(engineType), startingFuel);
            case 2 -> vehicle = new Motorcycle(make, model, year, new Engine(engineType), startingFuel);
            case 3 -> {
                double cargo = readDouble("Cargo capacity (tons): ");
                vehicle = new Truck(make, model, year, new Engine(engineType), startingFuel, cargo);
            }
            case 4 -> {
                int passengerCapacity = readInt("Passenger capacity: ");
                vehicle = new Bus(make, model, year, new Engine(engineType), startingFuel, passengerCapacity);
            }
            default -> {
                System.out.println("Not a valid type - vehicle not added.");
                return;
            }
        }

        fleet.add(vehicle); // Collections: growing the List via a method, like ListsDemo
        System.out.println("Added: " + vehicle);
    }

    private void driveVehicle() {
        Vehicle vehicle = selectVehicle();
        if (vehicle == null) return;
        int miles = readInt("Miles to drive: ");
        vehicle.drive(miles); // Encapsulation: validated inside Vehicle itself
    }

    private void refuelVehicle() {
        Vehicle vehicle = selectVehicle();
        if (vehicle == null) return;
        double gallons = readDouble("Gallons to add: ");
        vehicle.refuel(gallons); // Encapsulation: validated inside Vehicle itself
    }

    private void honkVehicle() {
        Vehicle vehicle = selectVehicle();
        if (vehicle == null) return;
        // Inheritance + polymorphism: this SAME line calls a different
        // honk() depending on which subclass `vehicle` actually is.
        System.out.println(vehicle.describe() + " says: " + vehicle.honk());
    }

    private Vehicle selectVehicle() {
        viewFleet();
        if (fleet.isEmpty()) return null;
        int index = readInt("Select a vehicle by index: ");
        if (index < 0 || index >= fleet.size()) {
            System.out.println("No vehicle at that index.");
            return null;
        }
        return fleet.get(index);
    }

    // --- Small input helpers --------------------------------------------
    // These just keep the menu from crashing on bad input. Real parameter
    // validation and our own exceptions arrive on Day 3.

    private int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            if (!scanner.hasNext()) {
                System.out.println("\nNo more input - exiting.");
                System.exit(0);
            }
            scanner.next();
            System.out.print("Please enter a whole number: ");
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private double readDouble(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            if (!scanner.hasNext()) {
                System.out.println("\nNo more input - exiting.");
                System.exit(0);
            }
            scanner.next();
            System.out.print("Please enter a number: ");
        }
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nNo more input - exiting.");
            System.exit(0);
        }
        return scanner.nextLine();
    }
}
