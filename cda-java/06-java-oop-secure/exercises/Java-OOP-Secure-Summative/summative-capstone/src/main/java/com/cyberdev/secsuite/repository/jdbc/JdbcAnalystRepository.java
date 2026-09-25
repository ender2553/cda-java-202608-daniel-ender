package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.repository.AnalystRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.AnalystRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO. Same pattern as SEC-3's JdbcAssetRepository
 * (read its class-level note on @Repository
@Profile("jdbc") without a container): constructor-injected
 * JdbcTemplate, "?" parameterized SQL only, explicit ORDER BY wherever order is promised, and
 * every org.springframework.dao.DataAccessException wrapped in this app's DataAccessException.
 *
 * Table: analyst. Note the error messages never include the password hash or the encrypted
 * email -- only the username.
 */
@Repository
@Profile("jdbc")
public class JdbcAnalystRepository implements AnalystRepository {

    private static final String SELECT_COLUMNS =
            "SELECT id, username, password_hash, encrypted_contact_email, created_at FROM analyst";

    private final JdbcTemplate jdbcTemplate;
    private final AnalystRowMapper rowMapper = new AnalystRowMapper();

    public JdbcAnalystRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Analyst save(Analyst analyst) {
        if (analyst == null) {
            throw new ValidationException("analyst must not be null");
        }
        try {
            Long id = jdbcTemplate.queryForObject(
                    "INSERT INTO analyst (username, password_hash, encrypted_contact_email, created_at) "
                            + "VALUES (?, ?, ?, ?) RETURNING id",
                    Long.class, analyst.getUsername(), analyst.getPasswordHash(),
                    analyst.getEncryptedContactEmail(), Timestamp.from(analyst.getCreatedAt()));
            return analyst.withId(id);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save analyst " + analyst.getUsername(), e);
        }
    }

    @Override
    public Optional<Analyst> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        try {
            List<Analyst> results = jdbcTemplate.query(SELECT_COLUMNS + " WHERE username = ?", rowMapper, username);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query analyst by username", e);
        }
    }

    @Override
    public Optional<Analyst> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            List<Analyst> results = jdbcTemplate.query(SELECT_COLUMNS + " WHERE id = ?", rowMapper, id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query analyst " + id, e);
        }
    }
}
