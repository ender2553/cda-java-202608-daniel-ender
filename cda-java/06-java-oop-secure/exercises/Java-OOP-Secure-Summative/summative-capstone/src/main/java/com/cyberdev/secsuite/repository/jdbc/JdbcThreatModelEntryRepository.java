package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.ThreatModelEntry;
import com.cyberdev.secsuite.repository.ThreatModelEntryRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.ThreatModelEntryRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * ThreatModelEntryRepository backed by the "threat_model_entry" table (SEC-8, persistence
 * half; the coverage half is ThreatModelingService.strideCoverage). See JdbcAssetRepository's
 * class note for why this is @Repository-annotated but constructed by hand.
 */
@Repository
@Profile("jdbc")
public class JdbcThreatModelEntryRepository implements ThreatModelEntryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ThreatModelEntryRowMapper rowMapper = new ThreatModelEntryRowMapper();

    public JdbcThreatModelEntryRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    // INSTRUCTOR NOTE [SEC-8]: save() is the given generated-id example, storing the two enums
    // by name() and passing the nullable mitigation through as a bound NULL.
    // findByThreatModelId: one "?" for the
    // model id, ORDER BY stride_category, description for a stable order, and an EMPTY list
    // when the model has no entries. What this repository must NOT do is try to "fill in"
    // missing STRIDE categories -- a repository returns what is stored; completeness over all
    // six categories is the service's job (strideCoverage), computed from the enum.
    @Override
    public ThreatModelEntry save(ThreatModelEntry entry) {
        if (entry == null) {
            throw new ValidationException("entry must not be null");
        }
        try {
            Long id = jdbcTemplate.queryForObject(
                    "INSERT INTO threat_model_entry (threat_model_id, stride_category, description, mitigation, status) "
                            + "VALUES (?, ?, ?, ?, ?) RETURNING id",
                    Long.class, entry.getThreatModelId(), entry.getStrideCategory().name(),
                    entry.getDescription(), entry.getMitigation(), entry.getStatus().name());
            return entry.withId(id);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save threat model entry", e);
        }
    }

    @Override
    public List<ThreatModelEntry> findByThreatModelId(Long threatModelId) {
        throw new UnsupportedOperationException(
                "TODO [SEC-8]: parameterized SELECT for one model ORDER BY stride_category, description; empty list when none");
    }
}
