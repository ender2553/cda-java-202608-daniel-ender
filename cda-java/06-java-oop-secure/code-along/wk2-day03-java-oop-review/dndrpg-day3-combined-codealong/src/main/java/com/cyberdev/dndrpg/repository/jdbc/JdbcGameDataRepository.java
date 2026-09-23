package com.cyberdev.dndrpg.repository.jdbc;

import com.cyberdev.dndrpg.exception.DataAccessException;
import com.cyberdev.dndrpg.model.ClassDefinition;
import com.cyberdev.dndrpg.model.ItemTemplate;
import com.cyberdev.dndrpg.model.MonsterTemplate;
import com.cyberdev.dndrpg.repository.GameDataRepository;
import com.cyberdev.dndrpg.repository.jdbc.mapper.ClassDefinitionRowMapper;
import com.cyberdev.dndrpg.repository.jdbc.mapper.ItemTemplateRowMapper;
import com.cyberdev.dndrpg.repository.jdbc.mapper.MonsterTemplateRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * INSTRUCTOR REFERENCE IMPLEMENTATION (DATA-1..DATA-4 solved).
 *
 * Reads the exact same reference data as FileGameDataRepository, but
 * from PostgreSQL via Spring's JdbcTemplate instead of CSV files.
 * GameService needs ZERO changes to use this instead -- only the lines
 * in the composition root (Main.java) that decide which implementations
 * to construct.
 *
 * Compare this to a raw-JDBC version: JdbcTemplate takes
 * try-with-resources on Connection/PreparedStatement/ResultSet off your
 * plate entirely, and translates every SQLException into an unchecked
 * org.springframework.dao.DataAccessException for you. We still catch
 * that and re-wrap it in OUR OWN exception.DataAccessException, so the
 * rest of the application never has to know Spring is involved at all --
 * the same "wrap the underlying failure" discipline as the raw-JDBC
 * version, just with less boilerplate to get there.
 */
@Repository
@Profile("jdbc")
public final class JdbcGameDataRepository implements GameDataRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ClassDefinitionRowMapper classDefinitionRowMapper = new ClassDefinitionRowMapper();
    private final MonsterTemplateRowMapper monsterTemplateRowMapper = new MonsterTemplateRowMapper();
    private final ItemTemplateRowMapper itemTemplateRowMapper = new ItemTemplateRowMapper();

    public JdbcGameDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ClassDefinition> findAllClasses() {
        try {
            return jdbcTemplate.query(
                    "SELECT character_class, base_hp, base_attack, base_defense, description FROM game_class",
                    classDefinitionRowMapper);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load classes from database", e);
        }
    }

    @Override
    public List<MonsterTemplate> findAllMonsters() {
        try {
            return jdbcTemplate.query(
                    "SELECT id, name, hp, attack, defense, xp_reward, gold_reward, is_boss FROM monster_template",
                    monsterTemplateRowMapper);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load monsters from database", e);
        }
    }

    @Override
    public Optional<MonsterTemplate> findMonsterById(String id) {
        try {
            List<MonsterTemplate> results = jdbcTemplate.query(
                    "SELECT id, name, hp, attack, defense, xp_reward, gold_reward, is_boss "
                            + "FROM monster_template WHERE id = ?",
                    monsterTemplateRowMapper, id);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load monster '" + id + "' from database", e);
        }
    }

    @Override
    public List<ItemTemplate> findAllItems() {
        try {
            return jdbcTemplate.query(
                    "SELECT id, name, item_type, attack_bonus, defense_bonus, value FROM item_template",
                    itemTemplateRowMapper);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load items from database", e);
        }
    }
}
