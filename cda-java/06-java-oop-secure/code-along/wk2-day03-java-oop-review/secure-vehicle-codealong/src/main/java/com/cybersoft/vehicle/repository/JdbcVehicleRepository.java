package com.cybersoft.vehicle.repository;

import com.cybersoft.vehicle.model.VehicleStatus;
import com.cybersoft.vehicle.model.VehicleSummary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Uses JdbcTemplate placeholders: never concatenate untrusted values into SQL. */
@Repository
@Profile("jdbc")
public final class JdbcVehicleRepository implements VehicleRepository {
    private final JdbcTemplate jdbc;

    public JdbcVehicleRepository(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    @Override
    public List<VehicleSummary> findAll() {
        return jdbc.query(
                "SELECT id, vin, description, status FROM vehicle ORDER BY id",
                (resultSet, rowNumber) -> new VehicleSummary(
                        resultSet.getLong("id"),
                        resultSet.getString("vin"),
                        resultSet.getString("description"),
                        VehicleStatus.valueOf(resultSet.getString("status"))
                )
        );
    }

    @Override
    public Optional<VehicleSummary> findByVin(String vin) {
        var matches = jdbc.query(
                "SELECT id, vin, description, status FROM vehicle WHERE vin = ?",
                (resultSet, rowNumber) -> new VehicleSummary(
                        resultSet.getLong("id"),
                        resultSet.getString("vin"),
                        resultSet.getString("description"),
                        VehicleStatus.valueOf(resultSet.getString("status"))
                ),
                vin.trim().toUpperCase()
        );
        return matches.stream().findFirst();
    }

    @Override
    public void save(VehicleSummary vehicle) {
        jdbc.update(
                """
                INSERT INTO vehicle(vin, description, status)
                VALUES (?, ?, ?)
                ON CONFLICT (vin) DO UPDATE
                SET description = EXCLUDED.description, status = EXCLUDED.status
                """,
                vehicle.vin(),
                vehicle.description(),
                vehicle.status().name()
        );
    }
}
