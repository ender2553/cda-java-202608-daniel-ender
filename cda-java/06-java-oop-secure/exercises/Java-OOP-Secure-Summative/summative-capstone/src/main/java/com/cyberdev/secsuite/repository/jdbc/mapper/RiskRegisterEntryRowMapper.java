package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.RiskRegisterEntry;
import com.cyberdev.secsuite.model.RiskStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
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
 * Columns: risk_register_entry (id, asset_id, scan_finding_id, title, description, likelihood,
 * impact, risk_score, status, owner_analyst_id, due_date, created_at). scan_finding_id,
 * owner_analyst_id and due_date are nullable.
 */
public final class RiskRegisterEntryRowMapper implements RowMapper<RiskRegisterEntry> {

    @Override
    public RiskRegisterEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date dueDate = rs.getDate("due_date");
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new RiskRegisterEntry(
                rs.getObject("id", Long.class),
                rs.getObject("asset_id", Long.class),
                rs.getObject("scan_finding_id", Long.class),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("likelihood"),
                rs.getInt("impact"),
                rs.getInt("risk_score"),
                RiskStatus.parse(rs.getString("status")),
                rs.getObject("owner_analyst_id", Long.class),
                dueDate == null ? null : dueDate.toLocalDate(),
                createdAt == null ? null : createdAt.toInstant());
    }
}
