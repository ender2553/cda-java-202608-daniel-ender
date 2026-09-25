package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.repository.ThreatIntelAlertRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.ThreatIntelAlertRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * ThreatIntelAlertRepository backed by the "threat_intel_alert" table (SEC-9). See
 * JdbcAssetRepository's class note for why this is @Repository-annotated but constructed by
 * hand.
 */
@Repository
@Profile("jdbc")
public class JdbcThreatIntelAlertRepository implements ThreatIntelAlertRepository {

    private static final String SELECT_COLUMNS = "SELECT id, external_alert_id, source, indicator_type, "
            + "indicator_value, related_cve_id, severity, description, published_at, ingested_at FROM threat_intel_alert";

    private final JdbcTemplate jdbcTemplate;
    private final ThreatIntelAlertRowMapper rowMapper = new ThreatIntelAlertRowMapper();

    public JdbcThreatIntelAlertRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    // INSTRUCTOR NOTE [SEC-9]: Concept tested: SEC-3's pattern on the table that holds the most
    // UNTRUSTED data in the schema. save() is the given generated-id example. Every text
    // column here came from a third-party feed, which
    // is precisely why every value (external id, indicator value, free-text description) is a
    // bound "?" parameter and never part of the SQL text. Nullable values (related_cve_id,
    // description, published_at) are bound as NULL; timestamps go in via Timestamp.from(...)
    // and must be null-checked first. findByExternalAlertId is SEC-11's dedup lookup and must
    // be an exact "= ?" match; findByIndicatorType binds indicatorType.name(); count() uses
    // queryForObject("SELECT count(*) ...", Long.class) (count(*) always returns exactly one
    // row, so queryForObject is appropriate here, unlike for find-by-id). Every ordered list is
    // ORDER BY external_alert_id. Common mistakes: an unguarded Timestamp.from(null)
    // (NullPointerException for an alert with no publication date); an upsert
    // ("ON CONFLICT DO UPDATE") in save() -- deduplication policy belongs to the service
    // (SEC-11), and a repository that silently overwrites would defeat it.
    @Override
    public ThreatIntelAlert save(ThreatIntelAlert alert) {
        if (alert == null) {
            throw new ValidationException("alert must not be null");
        }
        try {
            Long id = jdbcTemplate.queryForObject(
                    "INSERT INTO threat_intel_alert (external_alert_id, source, indicator_type, indicator_value, "
                            + "related_cve_id, severity, description, published_at, ingested_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
                    Long.class, alert.getExternalAlertId(), alert.getSource(), alert.getIndicatorType().name(),
                    alert.getIndicatorValue(), alert.getRelatedCveId(), alert.getSeverity().name(),
                    alert.getDescription(),
                    alert.getPublishedAt() == null ? null : Timestamp.from(alert.getPublishedAt()),
                    Timestamp.from(alert.getIngestedAt()));
            return alert.withId(id);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save threat intel alert " + alert.getExternalAlertId(), e);
        }
    }

    @Override
    public Optional<ThreatIntelAlert> findByExternalAlertId(String externalAlertId) {
        throw new UnsupportedOperationException(
                "TODO [SEC-9]: exact parameterized match on external_alert_id; empty result -> Optional.empty()");
    }

    @Override
    public List<ThreatIntelAlert> findAll() {
        throw new UnsupportedOperationException(
                "TODO [SEC-9]: SELECT every alert ORDER BY external_alert_id");
    }

    @Override
    public List<ThreatIntelAlert> findByIndicatorType(IndicatorType indicatorType) {
        throw new UnsupportedOperationException(
                "TODO [SEC-9]: parameterized SELECT ... WHERE indicator_type = ? (bind name()) ORDER BY external_alert_id");
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException(
                "TODO [SEC-9]: SELECT count(*) via queryForObject(sql, Long.class)");
    }
}
