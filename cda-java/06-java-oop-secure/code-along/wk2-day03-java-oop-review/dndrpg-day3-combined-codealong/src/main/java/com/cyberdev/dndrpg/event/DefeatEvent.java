package com.cyberdev.dndrpg.event;

/** defeatedName was defeated by defeatedByName -- either the character or the monster. */
public record DefeatEvent(String defeatedName, String defeatedByName) implements GameEvent {
    public DefeatEvent {
        if (defeatedName == null || defeatedName.isBlank()) {
            throw new IllegalArgumentException("defeatedName must not be blank");
        }
        if (defeatedByName == null || defeatedByName.isBlank()) {
            throw new IllegalArgumentException("defeatedByName must not be blank");
        }
    }
}
