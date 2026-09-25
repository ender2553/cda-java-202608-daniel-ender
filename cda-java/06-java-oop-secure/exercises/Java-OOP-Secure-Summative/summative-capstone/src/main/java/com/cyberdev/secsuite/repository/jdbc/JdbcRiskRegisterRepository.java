package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.RiskRegisterEntry;
import com.cyberdev.secsuite.repository.RiskRegisterRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.RiskRegisterEntryRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.sql.Date;
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
 * Table: risk_register_entry. Nullable columns (scan_finding_id, description, owner_analyst_id,
 * due_date) are bound as Java nulls; JdbcTemplate asks the driver for the parameter's SQL type
 * so a typed NULL is sent.
 */
@Repository
@Profile("jdbc")
public class JdbcRiskRegisterRepository implements RiskRegisterRepository {

    private static final String SELECT_COLUMNS = "SELECT id, asset_id, scan_finding_id, title, description, "
            + "likelihood, impact, risk_score, status, owner_analyst_id, due_date, created_at FROM risk_register_entry";

    private final JdbcTemplate jdbcTemplate;
    private final RiskRegisterEntryRowMapper rowMapper = new RiskRegisterEntryRowMapper();

    public JdbcRiskRegisterRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RiskRegisterEntry save(RiskRegisterEntry entry) {
        if (entry == null) {
            throw new ValidationException("entry must not be null");
        }
        try {
            Long id = jdbcTemplate.queryForObject(
                    "INSERT INTO risk_register_entry (asset_id, scan_finding_id, title, description, likelihood, "
                            + "impact, risk_score, status, owner_analyst_id, due_date, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
                    Long.class, entry.getAssetId(), entry.getScanFindingId(), entry.getTitle(),
                    entry.getDescription(), entry.getLikelihood(), entry.getImpact(), entry.getRiskScore(),
                    entry.getStatus().name(), entry.getOwnerAnalystId(),
                    entry.getDueDate() == null ? null : Date.valueOf(entry.getDueDate()),
                    Timestamp.from(entry.getCreatedAt()));
            return entry.withId(id);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save risk register entry " + entry.getId(), e);
        }
    }

    @Override
    public Optional<RiskRegisterEntry> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            List<RiskRegisterEntry> results = jdbcTemplate.query(SELECT_COLUMNS + " WHERE id = ?", rowMapper, id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query risk register entry " + id, e);
        }
    }

    @Override
    public List<RiskRegisterEntry> findAll() {
        try {
            return jdbcTemplate.query(SELECT_COLUMNS + " ORDER BY risk_score DESC, title", rowMapper);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to list risk register", e);
        }
    }

    @Override
    public Optional<RiskRegisterEntry> findByScanFindingId(Long scanFindingId) {
        if (scanFindingId == null) {
            return Optional.empty();
        }
        try {
            List<RiskRegisterEntry> results = jdbcTemplate.query(SELECT_COLUMNS + " WHERE scan_finding_id = ?",
                    rowMapper, scanFindingId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query risk for scan finding " + scanFindingId, e);
        }
    }
}
