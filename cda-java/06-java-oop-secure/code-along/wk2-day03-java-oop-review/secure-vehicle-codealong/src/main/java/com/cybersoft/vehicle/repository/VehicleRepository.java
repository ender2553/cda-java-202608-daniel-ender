package com.cybersoft.vehicle.repository;
import com.cybersoft.vehicle.model.VehicleSummary;
import java.util.List; import java.util.Optional;
public interface VehicleRepository { List<VehicleSummary> findAll(); Optional<VehicleSummary> findByVin(String vin); void save(VehicleSummary vehicle); }
