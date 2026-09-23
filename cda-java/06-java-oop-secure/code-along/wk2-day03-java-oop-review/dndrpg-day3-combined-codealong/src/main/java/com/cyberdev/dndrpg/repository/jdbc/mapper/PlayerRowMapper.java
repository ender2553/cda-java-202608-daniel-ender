package com.cyberdev.dndrpg.repository.jdbc.mapper;

import com.cyberdev.dndrpg.model.Player;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * PROVIDED -- maps one row of player to a Player. Notice this class
 * never sees a plaintext password or plaintext email: password_hash and
 * encrypted_email pass straight through as opaque strings, exactly as
 * they're stored.
 */
public final class PlayerRowMapper implements RowMapper<Player> {
    @Override
    public Player mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Player(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("encrypted_email"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()
        );
    }
}
