package org.example;

public class Truck extends Vehicle {
    private final double cargoCapacityTons;

    public Truck(String make, String model, int year, Engine engine, double startingFuel,
                 double cargoCapacityTons) {
        super(make, model, year, engine, startingFuel);
        this.cargoCapacityTons = cargoCapacityTons;
    }

    public double getCargoCapacityTons() {
        return cargoCapacityTons;
    }

    @Override
    public String honk() {
        return "HOOONK!";
    }

    @Override
    protected double milesPerGallon() {
        return 15.0;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | cargo: %.1f tons", cargoCapacityTons);
    }
}
