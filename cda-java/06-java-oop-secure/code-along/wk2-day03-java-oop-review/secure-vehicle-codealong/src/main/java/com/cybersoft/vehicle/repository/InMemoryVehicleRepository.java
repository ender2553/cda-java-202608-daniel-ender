package com.cybersoft.vehicle.repository;

import com.cybersoft.vehicle.model.VehicleSummary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("!jdbc")
public final class InMemoryVehicleRepository implements VehicleRepository {
    private final Map<String, VehicleSummary> vehicles = new ConcurrentHashMap<>();

    @Override
    public List<VehicleSummary> findAll() {
        return List.copyOf(vehicles.values());
    }

    @Override
    public Optional<VehicleSummary> findByVin(String vin) {
        return Optional.ofNullable(vehicles.get(vin.trim().toUpperCase()));
    }

    @Override
    public void save(VehicleSummary vehicle) {
        Objects.requireNonNull(vehicle);
        vehicles.put(vehicle.vin(), vehicle);
    }
}
