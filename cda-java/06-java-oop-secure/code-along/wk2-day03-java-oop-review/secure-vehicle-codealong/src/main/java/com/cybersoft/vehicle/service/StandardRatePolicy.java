package com.cybersoft.vehicle.service;

import com.cybersoft.vehicle.model.Vehicle;
import org.springframework.stereotype.Service;

@Service
public final class StandardRatePolicy implements RatePolicy {
    @Override
    public double quote(Vehicle vehicle, int days) {
        if (vehicle == null || days <= 0) {
            throw new IllegalArgumentException("vehicle and positive days required");
        }
        return vehicle.dailyRate() * days;
    }
}
