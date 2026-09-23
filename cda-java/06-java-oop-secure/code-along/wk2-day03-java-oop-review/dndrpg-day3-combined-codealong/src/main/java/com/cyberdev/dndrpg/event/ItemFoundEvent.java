package com.cyberdev.dndrpg.event;

/** characterName found itemName as a drop from a defeated (non-boss) monster. */
public record ItemFoundEvent(String characterName, String itemName) implements GameEvent {
    public ItemFoundEvent {
        if (characterName == null || characterName.isBlank()) {
            throw new IllegalArgumentException("characterName must not be blank");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("itemName must not be blank");
        }
    }
}
