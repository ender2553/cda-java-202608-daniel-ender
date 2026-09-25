package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.FindingStatus;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.repository.ScanFindingRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.ScanFindingRowMapper;
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
 * Table: scan_finding. updateStatus is the one UPDATE statement in the application (see the
 * UPDATE grant in dcl.sql); there is no delete method anywhere.
 */
@Repository
@Profile("jdbc")
public class JdbcScanFindingRepository implements ScanFindingRepository {

    private static final String SELECT_COLUMNS =
            "SELECT id, asset_id, cve_id, port, service_name, detected_at, status FROM scan_finding";
    private static final String ORDER = " ORDER BY detected_at, id";

    private final JdbcTemplate jdbcTemplate;
    private final ScanFindingRowMapper rowMapper = new ScanFindingRowMapper();

    public JdbcScanFindingRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ScanFinding save(ScanFinding finding) {
        if (finding == null) {
            throw new ValidationException("finding must not be null");
        }
        try {
            Long id = jdbcTemplate.queryForObject(
                    "INSERT INTO scan_finding (asset_id, cve_id, port, service_name, detected_at, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
                    Long.class, finding.getAssetId(), finding.getCveId(), finding.getPort(),
                    finding.getServiceName(), Timestamp.from(finding.getDetectedAt()), finding.getStatus().name());
            return finding.withId(id);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save scan finding " + finding.getId(), e);
        }
    }

    @Override
    public Optional<ScanFinding> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            List<ScanFinding> results = jdbcTemplate.query(SELECT_COLUMNS + " WHERE id = ?", rowMapper, id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query scan finding " + id, e);
        }
    }

    @Override
    public List<ScanFinding> findAll() {
        try {
            return jdbcTemplate.query(SELECT_COLUMNS + ORDER, rowMapper);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to list scan findings", e);
        }
    }

    @Override
    public List<ScanFinding> findByAssetId(Long assetId) {
        if (assetId == null) {
            return List.of();
        }
        try {
            return jdbcTemplate.query(SELECT_COLUMNS + " WHERE asset_id = ?" + ORDER, rowMapper, assetId);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query scan findings for asset " + assetId, e);
        }
    }

    @Override
    public Optional<ScanFinding> findOpenByAssetAndCve(Long assetId, String cveId) {
        if (assetId == null || cveId == null) {
            return Optional.empty();
        }
        try {
            List<ScanFinding> results = jdbcTemplate.query(
                    SELECT_COLUMNS + " WHERE asset_id = ? AND cve_id = ? AND status = ?",
                    rowMapper, assetId, cveId, FindingStatus.OPEN.name());
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query open finding for " + cveId, e);
        }
    }

    @Override
    public List<ScanFinding> findOpenByCveId(String cveId) {
        if (cveId == null) {
            return List.of();
        }
        try {
            return jdbcTemplate.query(SELECT_COLUMNS + " WHERE cve_id = ? AND status = ?" + ORDER,
                    rowMapper, cveId, FindingStatus.OPEN.name());
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query open findings for " + cveId, e);
        }
    }

    @Override
    public boolean updateStatus(Long findingId, FindingStatus newStatus) {
        if (newStatus == null) {
            throw new ValidationException("newStatus must not be null");
        }
        if (findingId == null) {
            return false;
        }
        try {
            return jdbcTemplate.update("UPDATE scan_finding SET status = ? WHERE id = ?",
                    newStatus.name(), findingId) > 0;
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to update scan finding " + findingId, e);
        }
    }
}
