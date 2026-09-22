package com.cyberdev.pos.day2.jdbc.mapper;

import com.cyberdev.pos.day2.Merchant;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * GIVEN/EXAMPLE (not a graded TODO) -- shows the intended shape of a real Spring
 * org.springframework.jdbc.core.RowMapper<T> implementation against a real ResultSet.
 * Compare against TransactionRecordRowMapper (POS2-8), which is the graded counterpart
 * students must write themselves. Column names here match the "merchant" table in
 * schema/schema.sql.
 */
public final class MerchantRowMapper implements RowMapper<Merchant> {

    @Override
    public Merchant mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Merchant(rs.getString("merchant_id"), rs.getString("display_name"));
    }
}
