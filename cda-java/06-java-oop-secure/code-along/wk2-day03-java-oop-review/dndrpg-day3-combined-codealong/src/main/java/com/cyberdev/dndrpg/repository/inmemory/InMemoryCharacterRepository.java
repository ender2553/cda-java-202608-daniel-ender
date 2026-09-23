package com.cyberdev.dndrpg.repository.inmemory;

import com.cyberdev.dndrpg.model.PlayerCharacter;
import com.cyberdev.dndrpg.repository.CharacterRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("!jdbc")
public final class InMemoryCharacterRepository implements CharacterRepository {
    private final Map<UUID, PlayerCharacter> byId = new ConcurrentHashMap<>();

    @Override
    public void save(PlayerCharacter character) {
        byId.put(character.getId(), character);
    }

    @Override
    public List<PlayerCharacter> findByPlayerId(UUID playerId) {
        List<PlayerCharacter> result = new ArrayList<>();
        for (PlayerCharacter c : byId.values()) {
            if (c.getPlayerId().equals(playerId)) result.add(c);
        }
        return result;
    }
}
