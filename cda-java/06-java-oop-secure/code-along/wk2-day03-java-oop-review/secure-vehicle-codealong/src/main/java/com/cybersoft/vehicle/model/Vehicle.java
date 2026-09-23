package com.cybersoft.vehicle.model;

import java.util.List;
import java.util.Objects;

/** Sealed base class: only explicitly permitted vehicle types may extend it. */
public abstract sealed class Vehicle permits Car, Truck {
    private final long id;
    private final String vin;
    private final String make;
    private final String model;
    private final int year;
    private VehicleStatus status;
    private final List<String> serviceNotes;

    protected Vehicle(long id, String vin, String make, String model, int year, VehicleStatus status, List<String> serviceNotes) {
        if (id < 0) throw new IllegalArgumentException("id cannot be negative");
        this.vin = require(vin, "vin").toUpperCase();
        if (!this.vin.matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("VIN must be 17 valid characters");
        this.make = require(make, "make");
        this.model = require(model, "model");
        if (year < 1886 || year > 2100) throw new IllegalArgumentException("year is outside the accepted range");
        this.status = Objects.requireNonNull(status, "status is required");
        this.id = id;
        this.year = year;
        this.serviceNotes = serviceNotes == null ? List.of() : List.copyOf(serviceNotes); // COPY IN
    }

    private static String require(String v, String n) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(n + " is required");
        return v.trim();
    }

    public final long getId() {
        return id;
    }

    public final String getVin() {
        return vin;
    }

    public final String getMake() {
        return make;
    }

    public final String getModel() {
        return model;
    }

    public final int getYear() {
        return year;
    }

    public final VehicleStatus getStatus() {
        return status;
    }

    public final List<String> getServiceNotes() {
        return List.copyOf(serviceNotes);
    } // COPY OUT

    public final void changeStatus(VehicleStatus newStatus) {
        status = Objects.requireNonNull(newStatus);
    }

    public abstract String type();

    public abstract double dailyRate();

    public final String description() {
        return year + " " + make + " " + model + " (" + type() + ")";
    }
}
