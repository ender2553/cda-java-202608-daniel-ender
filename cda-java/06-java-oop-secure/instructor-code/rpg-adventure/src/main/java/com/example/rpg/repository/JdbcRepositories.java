package com.example.rpg.repository;

import com.example.rpg.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * JDBC adapter. Every value is bound through JdbcTemplate placeholders to prevent SQL injection.
 */
@Repository
@Profile("jdbc")
class JdbcUserRepository implements UserRepository {
    private final JdbcTemplate jdbc;

    JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UserAccount save(UserAccount u) {
        if (u.id() > 0) {
            jdbc.update("update rpg_user set username=?, password_hash=? where id=?", u.username(), u.passwordHash(), u.id());
            return u;
        }
        long id = jdbc.queryForObject("insert into rpg_user(username,password_hash) values (?,?) returning id", Long.class, u.username(), u.passwordHash());
        return new UserAccount(id, u.username(), u.passwordHash());
    }

    public Optional<UserAccount> findByUsername(String n) {
        return jdbc.query("select id,username,password_hash from rpg_user where username=?", rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(new UserAccount(rs.getLong(1), rs.getString(2), rs.getString(3)));
        }, n);
    }
}

/**
 * JDBC save adapter. JSON is limited to the inventory value objects; the rest remains typed SQL.
 */
@Repository
@Profile("jdbc")
class JdbcCharacterRepository implements CharacterRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    JdbcCharacterRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(SaveSnapshot s) {
        try {
            String json = mapper.writeValueAsString(s.inventory());
            jdbc.update("insert into character_save(user_id,character_name,level,experience,health,max_health,gold,location,equipped_weapon,equipped_armor,inventory_json) values (?,?,?,?,?,?,?,?,?,?,?) on conflict(user_id) do update set character_name=excluded.character_name,level=excluded.level,experience=excluded.experience,health=excluded.health,max_health=excluded.max_health,gold=excluded.gold,location=excluded.location,equipped_weapon=excluded.equipped_weapon,equipped_armor=excluded.equipped_armor,inventory_json=excluded.inventory_json,updated_at=current_timestamp", s.userId(), s.characterName(), s.level(), s.experience(), s.health(), s.maxHealth(), s.gold(), s.location(), s.weapon() == null ? null : s.weapon().id(), s.armor() == null ? null : s.armor().id(), json);
        } catch (Exception e) {
            throw new IllegalStateException("save failed", e);
        }
    }

    public Optional<SaveSnapshot> findByUserId(long id) {
        return jdbc.query("select character_name,level,experience,health,max_health,gold,location,inventory_json from character_save where user_id=?", rs -> {
            if (!rs.next()) return Optional.empty();
            try {
                List<Item> items = mapper.readValue(rs.getString(8), new TypeReference<>() {
                });
                return Optional.of(new SaveSnapshot(id, rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getString(7), items, null, null));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, id);
    }
}
