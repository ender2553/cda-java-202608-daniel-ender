package com.cyberdev.pos.day2.jdbc.mapper;

import com.cyberdev.pos.day2.TransactionRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

// NOTE [POS2-8]: Implements Spring's real org.springframework.jdbc.core.RowMapper<T>
// contract (mapRow(ResultSet, int)) against the "transaction" table from schema/schema.sql
// (columns: transaction_id, merchant_id, amount, memo, occurred_at). The graded behavior is
// (1) implementing RowMapper<TransactionRecord> correctly with the same column names the
// repository's SELECT projects, and (2) NOT crashing with a bare NullPointerException/
// ClassCastException when the memo column is SQL NULL -- ResultSet.getString(...) already
// returns Java null for a SQL NULL column (no special-casing needed, unlike the old fake-row
// API this replaces), and the TransactionRecord constructor already defaults a null memo to
// "", so mapRow just needs to pass the raw value through. Common mistake: assuming every
// column is always populated and not handling a null memo the same way the constructor does.
public final class TransactionRecordRowMapper implements RowMapper<TransactionRecord> {

    @Override
    public TransactionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TransactionRecord(
                rs.getString("transaction_id"),
                rs.getString("merchant_id"),
                rs.getBigDecimal("amount"),
                rs.getString("memo"),
                rs.getObject("occurred_at", OffsetDateTime.class).toInstant()
        );
    }
}
