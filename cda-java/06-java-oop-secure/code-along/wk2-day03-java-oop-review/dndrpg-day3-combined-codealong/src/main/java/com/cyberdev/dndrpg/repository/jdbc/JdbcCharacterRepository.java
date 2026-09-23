package com.cyberdev.dndrpg.repository.jdbc;

import com.cyberdev.dndrpg.exception.DataAccessException;
import com.cyberdev.dndrpg.model.PlayerCharacter;
import com.cyberdev.dndrpg.repository.CharacterRepository;
import com.cyberdev.dndrpg.repository.GameDataRepository;
import com.cyberdev.dndrpg.repository.jdbc.mapper.PlayerCharacterRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * INSTRUCTOR REFERENCE IMPLEMENTATION (DATA-7, DATA-8 solved).
 *
 * save() is an upsert (INSERT ... ON CONFLICT (id) DO UPDATE) because,
 * unlike Player, a PlayerCharacter's stats change every encounter --
 * level, xp, gold, and current_hp all need to be overwritten on repeat
 * saves of the same character id.
 */
@Repository
@Profile("jdbc")
public final class JdbcCharacterRepository implements CharacterRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PlayerCharacterRowMapper rowMapper;

    public JdbcCharacterRepository(JdbcTemplate jdbcTemplate, GameDataRepository gameDataRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new PlayerCharacterRowMapper(gameDataRepository);
    }

    @Override
    public void save(PlayerCharacter character) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO player_character (id, player_id, name, character_class, level, xp, gold, current_hp) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                            + "ON CONFLICT (id) DO UPDATE SET "
                            + "level = EXCLUDED.level, xp = EXCLUDED.xp, "
                            + "gold = EXCLUDED.gold, current_hp = EXCLUDED.current_hp",
                    character.getId(), character.getPlayerId(), character.getName(),
                    character.getCharacterClass().name(), character.getLevel(), character.getXp(),
                    character.getGold(), character.getHp());
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save character " + character.getName(), e);
        }
    }

    @Override
    public List<PlayerCharacter> findByPlayerId(UUID playerId) {
        try {
            return jdbcTemplate.query(
                    "SELECT id, player_id, name, character_class, level, xp, gold, current_hp "
                            + "FROM player_character WHERE player_id = ?",
                    rowMapper, playerId);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load characters for player " + playerId, e);
        }
    }
}
