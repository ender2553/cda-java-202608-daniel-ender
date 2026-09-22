package com.cyberdev.dndrpg.repository;

import com.cyberdev.dndrpg.model.PlayerCharacter;

import java.util.List;
import java.util.UUID;

public interface CharacterRepository {
    void save(PlayerCharacter character);
    List<PlayerCharacter> findByPlayerId(UUID playerId);
}
