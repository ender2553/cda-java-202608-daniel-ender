package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Maps one "threat_intel_alert" row to a ThreatIntelAlert (SEC-9).
 *
 * Columns: threat_intel_alert (id, external_alert_id, source, indicator_type, indicator_value,
 * related_cve_id, severity, description, published_at, ingested_at). related_cve_id,
 * description and published_at are nullable.
 */
public final class ThreatIntelAlertRowMapper implements RowMapper<ThreatIntelAlert> {

    // INSTRUCTOR NOTE [SEC-9]: Same RowMapper contract as SEC-3, with the most nullable columns
    // of any table: read related_cve_id/description as plain getString (null stays null) and
    // published_at through getTimestamp with a null check before toInstant() -- calling
    // rs.getTimestamp("published_at").toInstant() unguarded NPEs on the first alert with no
    // publication date. severity goes through Severity.parseStoredAlertSeverity (NONE is not a
    // storable alert severity -- see Severity's design note); indicator_type through
    // IndicatorType.parse.

    @Override
    public ThreatIntelAlert mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getObject("id", Long.class);

        String externalAlertId = rs.getString("external_alert_id");
        String source = rs.getString("source");

        IndicatorType indicatorType =
                IndicatorType.parse(rs.getString("indicator_type"));

        String indicatorValue = rs.getString("indicator_value");
        String relatedCveId = rs.getString("related_cve_id");

        Severity severity =
                Severity.parseStoredAlertSeverity(rs.getString("severity"));

        String description = rs.getString("description");

        Timestamp publishedAtTimestamp =
                rs.getTimestamp("published_at");

        java.time.Instant publishedAt =
                publishedAtTimestamp == null
                        ? null
                        : publishedAtTimestamp.toInstant();

        Timestamp ingestedAtTimestamp =
                rs.getTimestamp("ingested_at");

        java.time.Instant ingestedAt =
                ingestedAtTimestamp == null
                        ? null
                        : ingestedAtTimestamp.toInstant();

        return new ThreatIntelAlert(
                id,
                externalAlertId,
                source,
                indicatorType,
                indicatorValue,
                relatedCveId,
                severity,
                description,
                publishedAt,
                ingestedAt
        );
    }
}
