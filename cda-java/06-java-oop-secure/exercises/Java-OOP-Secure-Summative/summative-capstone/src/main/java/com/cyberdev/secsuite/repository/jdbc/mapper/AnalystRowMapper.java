package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.Analyst;
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
 * Columns: analyst (id, username, password_hash, encrypted_contact_email, created_at).
 */
public final class AnalystRowMapper implements RowMapper<Analyst> {

    @Override
    public Analyst mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Analyst(
                rs.getObject("id", Long.class),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("encrypted_contact_email"),
                createdAt == null ? null : createdAt.toInstant());
    }
}
