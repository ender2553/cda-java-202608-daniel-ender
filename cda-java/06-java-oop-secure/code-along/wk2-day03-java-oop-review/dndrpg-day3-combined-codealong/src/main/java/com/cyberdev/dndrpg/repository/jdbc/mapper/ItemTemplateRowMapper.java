package com.cyberdev.dndrpg.repository.jdbc.mapper;

import com.cyberdev.dndrpg.model.ItemTemplate;
import com.cyberdev.dndrpg.model.ItemType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/** PROVIDED -- maps one row of item_template to an ItemTemplate. */
public final class ItemTemplateRowMapper implements RowMapper<ItemTemplate> {
    @Override
    public ItemTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ItemTemplate(
                rs.getString("id"),
                rs.getString("name"),
                ItemType.valueOf(rs.getString("item_type")),
                rs.getInt("attack_bonus"),
                rs.getInt("defense_bonus"),
                rs.getInt("value")
        );
    }
}
