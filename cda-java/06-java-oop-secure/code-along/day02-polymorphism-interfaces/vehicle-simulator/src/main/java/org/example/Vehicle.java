package org.example;

import java.util.Objects;

public abstract class Vehicle implements Transportable {
    private final String make;
    private final String model;
    private final int year;
    private final Engine engine;   // composition (HAS-A)
    private double fuelLevel;      // encapsulated, mutable state
    private int mileage;           // encapsulated, mutable state


    protected Vehicle(String make, String model, int year, Engine engine, double startingFuel) {
        if (make == null || make.isBlank()) {
            throw new IllegalArgumentException("make must not be null/blank");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model must not be null/blank");
        }
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("year must be between 1900 and 2100, was " + year);
        }
        if (engine == null) {
            throw new IllegalArgumentException("engine must not be null");
        }
        if (startingFuel < 0) {
            throw new IllegalArgumentException("startingFuel must not be negative");
        }
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
    public String move(int units) {
        double before = mileage;
        drive(units);
        boolean moved = mileage > before;
        return moved
                ? describe() + " moved " + units + " unit(s) under its own power."
                : describe() + " could NOT move " + units + " unit(s) - insufficient fuel.";
    }

    @Override
    public String getName() {
        return describe();
    }

    @Override
    public String toString() {
        return String.format("%s | %s engine | fuel: %.1f gal | mileage: %d mi",
                describe(), engine.getType(), fuelLevel, mileage);
    }


    @Override
    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Vehicle vehicle = (Vehicle) object;
        return year == vehicle.year
                && Objects.equals(make, vehicle.make)
                && Objects.equals(model, vehicle.model)
                && Objects.equals(getEngineType(), vehicle.getEngineType());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(make, model, year, getEngineType());
    }


}
