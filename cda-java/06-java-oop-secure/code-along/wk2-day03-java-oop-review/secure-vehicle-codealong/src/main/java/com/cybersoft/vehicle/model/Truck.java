package com.cybersoft.vehicle.model;
import java.util.List;

public final class Truck extends Vehicle {
    private final int payloadPounds;

    public Truck(long id, String vin, String make, String model, int year, VehicleStatus status, List<String> notes, int payloadPounds) {
        super(id, vin, make, model, year, status, notes);
        if (payloadPounds <= 0) throw new IllegalArgumentException("payload must be positive");
        this.payloadPounds = payloadPounds;
    }

    public int getPayloadPounds() {
        return payloadPounds;
    }

    @Override
    public String type() {
        return "Truck";
    }

    @Override
    public double dailyRate() {
        return 79.99;
    }
}
