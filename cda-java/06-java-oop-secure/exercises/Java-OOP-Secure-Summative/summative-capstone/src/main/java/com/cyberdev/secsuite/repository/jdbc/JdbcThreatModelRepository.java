package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.ThreatModel;
import com.cyberdev.secsuite.repository.ThreatModelRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.ThreatModelRowMapper;
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
 * Table: threat_model.
 */
@Repository
@Profile("jdbc")
public class JdbcThreatModelRepository implements ThreatModelRepository {

    private static final String SELECT_COLUMNS = "SELECT id, asset_id, title, description, created_at FROM threat_model";

    private final JdbcTemplate jdbcTemplate;
    private final ThreatModelRowMapper rowMapper = new ThreatModelRowMapper();

    public JdbcThreatModelRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ThreatModel save(ThreatModel threatModel) {
        if (threatModel == null) {
            throw new ValidationException("threatModel must not be null");
        }
        try {
            Long id = jdbcTemplate.queryForObject(
                    "INSERT INTO threat_model (asset_id, title, description, created_at) "
                            + "VALUES (?, ?, ?, ?) RETURNING id",
                    Long.class, threatModel.getAssetId(), threatModel.getTitle(),
                    threatModel.getDescription(), Timestamp.from(threatModel.getCreatedAt()));
            return threatModel.withId(id);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save threat model " + threatModel.getId(), e);
        }
    }

    @Override
    public Optional<ThreatModel> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            List<ThreatModel> results = jdbcTemplate.query(SELECT_COLUMNS + " WHERE id = ?", rowMapper, id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query threat model " + id, e);
        }
    }

    @Override
    public List<ThreatModel> findAll() {
        try {
            return jdbcTemplate.query(SELECT_COLUMNS + " ORDER BY title", rowMapper);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to list threat models", e);
        }
    }

    @Override
    public List<ThreatModel> findByAssetId(Long assetId) {
        if (assetId == null) {
            return List.of();
        }
        try {
            return jdbcTemplate.query(SELECT_COLUMNS + " WHERE asset_id = ? ORDER BY title", rowMapper, assetId);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query threat models for asset " + assetId, e);
        }
    }
}
