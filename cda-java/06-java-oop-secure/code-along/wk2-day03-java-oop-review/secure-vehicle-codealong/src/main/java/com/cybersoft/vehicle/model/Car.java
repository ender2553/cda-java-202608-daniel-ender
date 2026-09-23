package com.cybersoft.vehicle.model;
import java.util.List;

public final class Car extends Vehicle {
    private final int seats;

    public Car(long id, String vin, String make, String model, int year, VehicleStatus status, List<String> notes, int seats) {
        super(id, vin, make, model, year, status, notes);
        if (seats < 1 || seats > 9) throw new IllegalArgumentException("seats must be 1-9");
        this.seats = seats;
    }

    public int getSeats() {
        return seats;
    }

    @Override
    public String type() {
        return "Car";
    }

    @Override
    public double dailyRate() {
        return 49.99;
    }
}
