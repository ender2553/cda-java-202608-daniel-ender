package com.cyberdev.dndrpg.repository.jdbc.mapper;

import com.cyberdev.dndrpg.model.MonsterTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/** PROVIDED -- maps one row of monster_template to a MonsterTemplate. */
public final class MonsterTemplateRowMapper implements RowMapper<MonsterTemplate> {
    @Override
    public MonsterTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MonsterTemplate(
                rs.getString("id"),
                rs.getString("name"),
                rs.getInt("hp"),
                rs.getInt("attack"),
                rs.getInt("defense"),
                rs.getInt("xp_reward"),
                rs.getInt("gold_reward"),
                rs.getBoolean("is_boss")
        );
    }
}
