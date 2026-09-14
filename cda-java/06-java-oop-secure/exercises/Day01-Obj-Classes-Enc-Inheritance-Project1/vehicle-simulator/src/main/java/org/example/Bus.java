package org.example;

public class Bus extends Vehicle {

    private final int passengerCapacity;

    public Bus(String make, String model, int year, Engine engine,
               double startingFuel, int passengerCapacity) {
        super(make, model, year, engine, startingFuel);
        this.passengerCapacity = passengerCapacity;
    }

    @Override
    public String honk() {
        return "Bus goes: HONK HONK!";
    }

    @Override
    protected double milesPerGallon() {
        return 8.0;
    }

    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    @Override
    public String toString() {
        return String.format("%s | %s engine | fuel: %.1f gal | mileage: %d mi | passenger capacity: %d",
                describe(), getEngineType(), getFuelLevel(), getMileage(), passengerCapacity);
    }
}