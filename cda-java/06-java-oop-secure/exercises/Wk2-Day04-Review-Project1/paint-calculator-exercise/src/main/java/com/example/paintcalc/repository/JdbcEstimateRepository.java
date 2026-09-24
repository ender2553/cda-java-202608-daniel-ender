package com.example.paintcalc.repository;

import com.example.paintcalc.domain.PaintEstimate;
import com.example.paintcalc.domain.Room;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/** JDBC adapter demonstrating coding to the estimate repository interface. */
@Repository
@Profile("jdbc")
public class JdbcEstimateRepository implements EstimateRepository {
    private final JdbcTemplate jdbc;

    public JdbcEstimateRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(PaintEstimate estimate, Room room) {
        // TODO 7: insert the estimate with JdbcTemplate and parameter placeholders.
        // SECURITY: do not concatenate values into SQL.
        throw new UnsupportedOperationException("TODO: implement JdbcEstimateRepository.save");
    }

    @Override
    public List<PaintEstimate> findByUserId(long userId) {
        // TODO 8: map rows into PaintEstimate records and return the user's history.
        return List.of();
    }
}
