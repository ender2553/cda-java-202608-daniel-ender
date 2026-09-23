package com.cyberdev.dndrpg.repository.jdbc.mapper;

import com.cyberdev.dndrpg.model.CharacterClass;
import com.cyberdev.dndrpg.model.ClassDefinition;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PROVIDED -- maps one row of game_class to a ClassDefinition.
 *
 * A separate top-level class, rather than a lambda passed inline to
 * jdbcTemplate.query(...), so it can be unit tested on its own and
 * reused anywhere a ClassDefinition needs to come out of a ResultSet.
 * ClassDefinition's own compact constructor still validates every field
 * -- this class's only job is pulling columns out of the row.
 */
public final class ClassDefinitionRowMapper implements RowMapper<ClassDefinition> {
    @Override
    public ClassDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ClassDefinition(
                CharacterClass.valueOf(rs.getString("character_class")),
                rs.getInt("base_hp"),
                rs.getInt("base_attack"),
                rs.getInt("base_defense"),
                rs.getString("description")
        );
    }
}
