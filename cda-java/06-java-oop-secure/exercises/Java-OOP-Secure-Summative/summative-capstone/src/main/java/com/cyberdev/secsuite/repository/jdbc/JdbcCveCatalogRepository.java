package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.repository.CveCatalogRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.CveCatalogEntryRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO. Same pattern as SEC-3's JdbcAssetRepository
 * (read its class-level note on @Repository
@Profile("jdbc") without a container): constructor-injected
 * JdbcTemplate, "?" parameterized SQL only, explicit ORDER BY wherever order is promised, and
 * every org.springframework.dao.DataAccessException wrapped in this app's DataAccessException.
 *
 * Table: cve_catalog.
 */
@Repository
@Profile("jdbc")
public class JdbcCveCatalogRepository implements CveCatalogRepository {

    private static final String SELECT_COLUMNS = "SELECT cve_id, description, cvss_score FROM cve_catalog";

    private final JdbcTemplate jdbcTemplate;
    private final CveCatalogEntryRowMapper rowMapper = new CveCatalogEntryRowMapper();

    public JdbcCveCatalogRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(CveCatalogEntry entry) {
        if (entry == null) {
            throw new ValidationException("entry must not be null");
        }
        try {
            jdbcTemplate.update("INSERT INTO cve_catalog (cve_id, description, cvss_score) VALUES (?, ?, ?)",
                    entry.cveId(), entry.description(), entry.cvssScore());
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save CVE " + entry.cveId(), e);
        }
    }

    @Override
    public Optional<CveCatalogEntry> findById(String cveId) {
        if (cveId == null) {
            return Optional.empty();
        }
        try {
            List<CveCatalogEntry> results = jdbcTemplate.query(SELECT_COLUMNS + " WHERE cve_id = ?", rowMapper, cveId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query CVE " + cveId, e);
        }
    }

    @Override
    public List<CveCatalogEntry> findAll() {
        try {
            return jdbcTemplate.query(SELECT_COLUMNS + " ORDER BY cve_id", rowMapper);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to list CVE catalog", e);
        }
    }
}
