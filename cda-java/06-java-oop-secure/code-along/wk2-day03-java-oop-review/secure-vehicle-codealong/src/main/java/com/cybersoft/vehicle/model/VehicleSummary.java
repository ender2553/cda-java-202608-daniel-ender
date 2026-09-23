package com.cybersoft.vehicle.model;
/** Immutable DTO: a record is ideal for data transfer/read models. */
public record VehicleSummary(long id, String vin, String description, VehicleStatus status) {


    public VehicleSummary {
        if (id < 0) throw new IllegalArgumentException("id cannot be negative");
        vin = require(vin, "vin").toUpperCase();
        description = require(description, "description");
        if (status == null) throw new IllegalArgumentException("status is required");

    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }
}
