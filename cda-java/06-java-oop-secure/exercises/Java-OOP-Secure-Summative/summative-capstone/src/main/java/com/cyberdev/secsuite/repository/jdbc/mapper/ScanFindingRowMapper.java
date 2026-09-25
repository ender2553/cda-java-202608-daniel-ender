package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.FindingStatus;
import com.cyberdev.secsuite.model.ScanFinding;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * GIVEN/EXAMPLE (not a graded TODO) -- same shape as QuickPay's MerchantRowMapper: a real
 * {@code org.springframework.jdbc.core.RowMapper<T>} that turns ONE row of a real ResultSet into ONE
 * model object, reading columns by NAME (never by position) and going through the model's
 * validating constructor, so a corrupt row fails closed as a ValidationException instead of
 * producing an invalid object.
 *
 * Columns: scan_finding (id, asset_id, cve_id, port, service_name, detected_at, status). port is
 * nullable, so it is read with getObject(..., Integer.class) -- rs.getInt would silently turn
 * SQL NULL into 0, which is a real (and misleading) port number.
 */
public final class ScanFindingRowMapper implements RowMapper<ScanFinding> {

    @Override
    public ScanFinding mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp detectedAt = rs.getTimestamp("detected_at");
        return new ScanFinding(
                rs.getObject("id", Long.class),
                rs.getObject("asset_id", Long.class),
                rs.getString("cve_id"),
                rs.getObject("port", Integer.class),
                rs.getString("service_name"),
                detectedAt == null ? null : detectedAt.toInstant(),
                FindingStatus.parse(rs.getString("status")));
    }
}
