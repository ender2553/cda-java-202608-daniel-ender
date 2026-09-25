package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.Criticality;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps one "asset" row to an Asset (SEC-3).
 *
 * Columns: asset (id, hostname, ip_address, owner_team, criticality).
 */
public final class AssetRowMapper implements RowMapper<Asset> {

    // INSTRUCTOR NOTE [SEC-3]: Concept tested: implementing Spring's RowMapper<T> contract --
    // mapRow is called ONCE PER ROW with the ResultSet already positioned on that row, so it
    // must NOT call rs.next() itself (a classic mistake that silently skips every other row).
    // Read every column BY NAME (rs.getString("hostname"), not rs.getString(2): positional
    // reads break the day someone reorders the SELECT list), convert the Long with
    // rs.getObject("id", Long.class), convert the criticality text with the strict
    // Criticality.parse (an unexpected value fails closed -- it does not become LOW), and build
    // the object through the SEC-1 validating constructor. Common mistakes: rs.next() inside
    // mapRow; Criticality.valueOf without handling bad data; parsing ids from text instead of
    // reading them as Long values
    // (works, but NPEs on NULL and hides the driver's native Long support); declaring the
    // mapper to catch and swallow SQLException -- let it propagate, JdbcTemplate translates it.
    @Override
    public Asset mapRow(ResultSet rs, int rowNum) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO [SEC-3]: map the current row to an Asset, reading every column by name (never call rs.next())");
    }
}
