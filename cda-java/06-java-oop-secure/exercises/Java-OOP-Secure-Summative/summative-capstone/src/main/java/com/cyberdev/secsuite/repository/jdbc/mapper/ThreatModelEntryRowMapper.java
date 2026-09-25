package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.StrideCategory;
import com.cyberdev.secsuite.model.ThreatModelEntry;
import com.cyberdev.secsuite.model.ThreatModelEntryStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps one "threat_model_entry" row to a ThreatModelEntry (SEC-8).
 *
 * Columns: threat_model_entry (id, threat_model_id, stride_category, description, mitigation,
 * status). mitigation is nullable.
 */
public final class ThreatModelEntryRowMapper implements RowMapper<ThreatModelEntry> {

    // INSTRUCTOR NOTE [SEC-8]: Same RowMapper contract as SEC-3. The interesting part is the
    // two enum columns: stride_category and status are VARCHARs in the database, converted with
    // the strict StrideCategory.parse / ThreatModelEntryStatus.parse. If the database ever held
    // a value outside the enum (someone relaxed the CHECK constraint, or a typo'd manual
    // insert), mapping fails closed with ValidationException rather than guessing a category --
    // a threat silently re-filed under the wrong STRIDE letter would corrupt SEC-8's coverage
    // report. mitigation may be NULL; pass it through (the model normalizes blank to null).
    @Override
    public ThreatModelEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getObject("id", Long.class);
        Long threatModelId = rs.getObject("threat_model_id", Long.class);
        StrideCategory strideCategory =
                StrideCategory.parse(rs.getString("stride_category"));
        String description = rs.getString("description");
        String mitigation = rs.getString("mitigation");
        ThreatModelEntryStatus status =
                ThreatModelEntryStatus.parse(rs.getString("status"));

        return new ThreatModelEntry(
                id,
                threatModelId,
                strideCategory,
                description,
                mitigation,
                status
        );
    }
}
