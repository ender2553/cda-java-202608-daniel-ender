package com.cyberdev.dndrpg.repository.jdbc.mapper;

import com.cyberdev.dndrpg.model.CharacterClass;
import com.cyberdev.dndrpg.model.ClassDefinition;
import com.cyberdev.dndrpg.model.PlayerCharacter;
import com.cyberdev.dndrpg.repository.GameDataRepository;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * PROVIDED -- maps one row of player_character back into a
 * PlayerCharacter via PlayerCharacter.reconstitute(...).
 *
 * This mapper needs a ClassDefinition to rebuild the character, so it
 * takes a GameDataRepository through its own constructor -- the same
 * constructor-injection discipline used everywhere else in this
 * project, applied to a RowMapper this time instead of a service.
 */
public final class PlayerCharacterRowMapper implements RowMapper<PlayerCharacter> {

    private final GameDataRepository gameDataRepository;

    public PlayerCharacterRowMapper(GameDataRepository gameDataRepository) {
        if (gameDataRepository == null) {
            throw new IllegalArgumentException("gameDataRepository must not be null");
        }
        this.gameDataRepository = gameDataRepository;
    }

    @Override
    public PlayerCharacter mapRow(ResultSet rs, int rowNum) throws SQLException {
        CharacterClass characterClass = CharacterClass.valueOf(rs.getString("character_class"));
        ClassDefinition definition = gameDataRepository.findAllClasses().stream()
                .filter(c -> c.characterClass() == characterClass)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Unknown character_class in database row: " + characterClass));

        return PlayerCharacter.reconstitute(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("player_id")),
                rs.getString("name"),
                definition,
                rs.getInt("level"),
                rs.getInt("xp"),
                rs.getInt("gold"),
                rs.getInt("current_hp")
        );
    }
}
