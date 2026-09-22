package com.cyberdev.pos.day2.jdbc.mapper;

import com.cyberdev.pos.day2.TransactionRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

// TODO [POS2-8]: Implement Spring's real RowMapper<T> contract (mapRow(ResultSet rs, int
// rowNum)) against the "transaction" table from schema/schema.sql. Columns: transaction_id
// (String), merchant_id (String), amount (BigDecimal -- use rs.getBigDecimal(...)), memo
// (String, may be SQL NULL), occurred_at (a TIMESTAMPTZ -- read it as
// rs.getObject("occurred_at", java.time.OffsetDateTime.class).toInstant() to get an
// Instant). Build and return a new TransactionRecord from those five values.
//
// A missing/NULL memo column must NOT throw -- ResultSet.getString(...) already returns
// Java null for a SQL NULL, and TransactionRecord's constructor already defaults a null
// memo to "" for you, so just pass rs.getString("memo") straight through.
public final class TransactionRecordRowMapper implements RowMapper<TransactionRecord> {

    @Override
    public TransactionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO [POS2-8]: map a ResultSet row to a TransactionRecord (see schema/schema.sql for column names)");
    }
}
