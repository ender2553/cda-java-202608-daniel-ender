package org.example;

public class Boat extends Vehicle {
    private final String hullType; // e.g. "monohull", "catamaran"

    public Boat(String make, String model, int year, Engine engine, double startingFuel,
                String hullType) {
        super(make, model, year, engine, startingFuel);
        if (hullType == null || hullType.isBlank()) {
            throw new IllegalArgumentException("hullType must not be null/blank");
        }
        this.hullType = hullType;
    }

    public String getHullType() {
        return hullType;
    }

    @Override
    public String honk() {
        return "Toot toot! (ship horn, two short blasts)";
    }

    @Override
    protected double milesPerGallon() {
        return 8.0;
    }

    @Override
    public String toString() {
        return super.toString() + " | hull: " + hullType;
    }
}
