package org.example;

public class Plane extends Vehicle {
    private final int cruisingAltitudeFeet;

    public Plane(String make, String model, int year, Engine engine, double startingFuel,
                 int cruisingAltitudeFeet) {
        super(make, model, year, engine, startingFuel);
        if (cruisingAltitudeFeet < 0) {
            throw new IllegalArgumentException("cruisingAltitudeFeet must not be negative");
        }
        this.cruisingAltitudeFeet = cruisingAltitudeFeet;
    }

    public int getCruisingAltitudeFeet() {
        return cruisingAltitudeFeet;
    }

    @Override
    public String honk() {
        return "Wooosh! (jet engines spooling up, cleared for takeoff)";
    }

    @Override
    protected double milesPerGallon() {
        return 5.0; // jet fuel burns fast per mile compared to a car
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | cruising altitude: %,d ft", cruisingAltitudeFeet);
    }
}
