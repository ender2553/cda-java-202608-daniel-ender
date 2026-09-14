package org.example;
/**
 * Motorcycle.java
 * ----------------
 * Inheritance: extends Vehicle with its own honk() and milesPerGallon().
 */
public class Motorcycle extends Vehicle {

    public Motorcycle(String make, String model, int year, Engine engine, double startingFuel) {
        super(make, model, year, engine, startingFuel);
    }

    @Override
    public String honk() {
        return "Honk! (surprisingly loud for something this small)";
    }

    @Override
    protected double milesPerGallon() {
        return 45.0;
    }
}
