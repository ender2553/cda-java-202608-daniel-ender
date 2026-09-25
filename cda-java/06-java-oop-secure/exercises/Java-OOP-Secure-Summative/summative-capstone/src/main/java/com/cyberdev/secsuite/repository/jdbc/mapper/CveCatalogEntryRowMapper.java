package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.CveCatalogEntry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * GIVEN/EXAMPLE (not a graded TODO) -- same shape as QuickPay's MerchantRowMapper: a real
 * {@code org.springframework.jdbc.core.RowMapper<T>} that turns ONE row of a real ResultSet into ONE
 * model object, reading columns by NAME (never by position) and going through the model's
 * validating constructor, so a corrupt row fails closed as a ValidationException instead of
 * producing an invalid object.
 *
 * Columns: cve_catalog (cve_id, description, cvss_score). cvss_score is NUMERIC(3,1), read as a
 * BigDecimal (never as a double -- the exact decimal matters at band boundaries like 6.9/7.0).
 */
public final class CveCatalogEntryRowMapper implements RowMapper<CveCatalogEntry> {

    @Override
    public CveCatalogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CveCatalogEntry(rs.getString("cve_id"), rs.getString("description"), rs.getBigDecimal("cvss_score"));
    }
}
