package com.cyberdev.dndrpg.event;

/** characterName just reached newLevel. */
public record LevelUpEvent(String characterName, int newLevel) implements GameEvent {
    public LevelUpEvent {
        if (characterName == null || characterName.isBlank()) {
            throw new IllegalArgumentException("characterName must not be blank");
        }
        if (newLevel < 1) {
            throw new IllegalArgumentException("newLevel must be at least 1");
        }
    }
}
