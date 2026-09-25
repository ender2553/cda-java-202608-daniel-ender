package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.ThreatModel;
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
 * Columns: threat_model (id, asset_id, title, description, created_at).
 */
public final class ThreatModelRowMapper implements RowMapper<ThreatModel> {

    @Override
    public ThreatModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new ThreatModel(
                rs.getObject("id", Long.class),
                rs.getObject("asset_id", Long.class),
                rs.getString("title"),
                rs.getString("description"),
                createdAt == null ? null : createdAt.toInstant());
    }
}
