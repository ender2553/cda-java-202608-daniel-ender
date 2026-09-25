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
        throw new UnsupportedOperationException(
                "TODO [SEC-9]: map the current row to a ThreatIntelAlert, null-safe for related_cve_id/description/published_at");
    }
}
