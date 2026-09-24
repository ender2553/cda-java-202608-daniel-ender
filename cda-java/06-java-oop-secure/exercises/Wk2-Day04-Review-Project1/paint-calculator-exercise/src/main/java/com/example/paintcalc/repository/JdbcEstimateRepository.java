package com.example.paintcalc.repository;

import com.example.paintcalc.domain.PaintEstimate;
import com.example.paintcalc.domain.PaintColor;
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

        jdbc.update(
                """
                INSERT INTO paint_estimate
                    (user_id, room_length, room_width, room_height,
                     coats, color, gallons, paint_cost)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                estimate.userId(),
                room.length(),
                room.width(),
                room.height(),
                estimate.coats(),
                estimate.color().name(),
                estimate.gallons(),
                estimate.cost()
        );
    }

    @Override
    public List<PaintEstimate> findByUserId(long userId) {
        // TODO 8: map rows into PaintEstimate records and return the user's history.

        return jdbc.query(
                """
                SELECT user_id, room_length, room_width, room_height,
                       coats, color, gallons, paint_cost
                FROM paint_estimate
                WHERE user_id = ?
                ORDER BY created_at DESC
                """,
                (rs, rowNum) -> {
                    Room room = new Room(
                            rs.getBigDecimal("room_length"),
                            rs.getBigDecimal("room_width"),
                            rs.getBigDecimal("room_height")
                    );

                    return new PaintEstimate(
                            rs.getLong("user_id"),
                            room.wallSquareFeet(),
                            rs.getInt("coats"),
                            PaintColor.valueOf(rs.getString("color")),
                            rs.getInt("gallons"),
                            rs.getBigDecimal("paint_cost")
                    );
                },
                userId
        );
    }
}

