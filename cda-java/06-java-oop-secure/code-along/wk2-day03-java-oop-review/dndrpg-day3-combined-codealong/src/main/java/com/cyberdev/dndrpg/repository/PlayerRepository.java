package com.cyberdev.dndrpg.repository;

import com.cyberdev.dndrpg.model.Player;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository {
    void save(Player player);
    Optional<Player> findByUsername(String username);
    Optional<Player> findById(UUID id);
}
