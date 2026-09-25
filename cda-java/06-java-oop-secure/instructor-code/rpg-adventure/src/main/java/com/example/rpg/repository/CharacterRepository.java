package com.example.rpg.repository;
import com.example.rpg.domain.SaveSnapshot;
import java.util.Optional;
/** Persistence port for immutable save snapshots. */
public interface CharacterRepository { void save(SaveSnapshot save); Optional<SaveSnapshot> findByUserId(long userId); }
