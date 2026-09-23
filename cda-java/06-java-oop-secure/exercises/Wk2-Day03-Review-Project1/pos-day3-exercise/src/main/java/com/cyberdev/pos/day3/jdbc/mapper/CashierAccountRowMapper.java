package com.cyberdev.pos.day3.jdbc.mapper;

import com.cyberdev.pos.day3.CashierAccount;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * Mirrors Day 2's MerchantRowMapper/TransactionRecordRowMapper shape. Column names match
 * the "cashier_account" table in schema/day3_schema.sql (cashier_id, salt, pin_hash, both
 * BYTEA columns read back as byte[] via ResultSet.getBytes).
 */
public final class CashierAccountRowMapper implements RowMapper<CashierAccount> {

    @Override
    public CashierAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CashierAccount(
                rs.getString("cashier_id"),
                rs.getBytes("salt"),
                rs.getBytes("pin_hash"));
    }
}
