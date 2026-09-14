package org.example;
/**
 * Vehicle.java
 * =============
 * The shared parent for every vehicle type in the simulator. This one
 * class is where Day 1's four OOP concepts all meet:
 *
 *   Classes & Objects : fields, a constructor, and methods
 *   Encapsulation     : private fields, changed only through validated
 *                       methods (drive(), refuel())
 *   Inheritance       : Car / Motorcycle / Truck all extend this class
 *                       and override honk() and milesPerGallon()
 *   Composition       : every Vehicle HAS-A Engine, composed in rather
 *                       than inherited
 *
 * GROWTH ROADMAP — future days will build directly on this class:
 *   - Methods that validate parameters + exceptions that fail closed
 *     instead of just printing a rejection message
 *   - Allow-list validation for a vehicle "type" or "engineType" field
 *   - A Drivable interface so Plane, Boat, Spaceship, and Train can join
 *     the simulator without extending Vehicle directly
 *   - A repository that persists vehicles instead of holding them in
 *     an in-memory List
 */
public abstract class Vehicle {
    private final String make;
    private final String model;
    private final int year;
    private final Engine engine;   // composition (HAS-A)
    private double fuelLevel;      // encapsulated, mutable state
    private int mileage;           // encapsulated, mutable state

    protected Vehicle(String make, String model, int year, Engine engine, double startingFuel) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.engine = engine;
        this.fuelLevel = startingFuel;
        this.mileage = 0;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public int getMileage() {
        return mileage;
    }

    public String getEngineType() {
        return engine.getType();
    }

    // Every Vehicle can honk, but HOW differs per subclass (inheritance).
    public abstract String honk();

    // Every Vehicle has its own fuel efficiency (inheritance).
    protected abstract double milesPerGallon();

    // Encapsulation: fuelLevel and mileage change ONLY through this method.
    public void drive(int miles) {
        double gallonsNeeded = miles / milesPerGallon();
        if (gallonsNeeded > fuelLevel) {
            System.out.println(describe() + " can't drive " + miles + " miles - not enough fuel.");
            return;
        }
        fuelLevel -= gallonsNeeded;
        mileage += miles;
        System.out.println(describe() + " drove " + miles + " miles. " + engine.start());
    }

    // Encapsulation: the ONLY door in for adding fuel.
    public void refuel(double gallons) {
        if (gallons <= 0) {
            System.out.println("Refuel amount must be positive.");
            return;
        }
        fuelLevel += gallons;
        System.out.printf("%s refueled. Fuel level: %.1f gallons%n", describe(), fuelLevel);
    }

    public String describe() {
        return year + " " + make + " " + model;
    }

    @Override
    public String toString() {
        return String.format("%s | %s engine | fuel: %.1f gal | mileage: %d mi",
                describe(), engine.getType(), fuelLevel, mileage);
    }
}
