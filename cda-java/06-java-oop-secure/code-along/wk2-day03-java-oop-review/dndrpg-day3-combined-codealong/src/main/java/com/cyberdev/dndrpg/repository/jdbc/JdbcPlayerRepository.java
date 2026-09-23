package com.cyberdev.dndrpg.repository.jdbc;

import com.cyberdev.dndrpg.exception.DataAccessException;
import com.cyberdev.dndrpg.model.Player;
import com.cyberdev.dndrpg.repository.PlayerRepository;
import com.cyberdev.dndrpg.repository.jdbc.mapper.PlayerRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * INSTRUCTOR REFERENCE IMPLEMENTATION (DATA-5, DATA-6 solved).
 * Common student mistakes to watch for: forgetting Timestamp.from(...)
 * on the Instant (JdbcTemplate won't auto-convert it); using
 * queryForObject and having to catch Spring's
 * EmptyResultDataAccessException instead of the query()+isEmpty()
 * pattern used here, which reads more clearly for an Optional-returning
 * method.
 */
@Repository
@Profile("jdbc")
public final class JdbcPlayerRepository implements PlayerRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PlayerRowMapper rowMapper = new PlayerRowMapper();

    public JdbcPlayerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Player player) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO player (id, username, password_hash, encrypted_email, created_at) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    player.getId(), player.getUsername(), player.getPasswordHash(),
                    player.getEncryptedEmail(), Timestamp.from(player.getCreatedAt()));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save player " + player.getUsername(), e);
        }
    }

    @Override
    public Optional<Player> findByUsername(String username) {
        try {
            List<Player> results = jdbcTemplate.query(
                    "SELECT id, username, password_hash, encrypted_email, created_at "
                            + "FROM player WHERE username = ?",
                    rowMapper, username);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load player by username " + username, e);
        }
    }

    @Override
    public Optional<Player> findById(UUID id) {
        try {
            List<Player> results = jdbcTemplate.query(
                    "SELECT id, username, password_hash, encrypted_email, created_at "
                            + "FROM player WHERE id = ?",
                    rowMapper, id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load player by id " + id, e);
        }
    }
}
