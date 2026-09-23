package com.cyberdev.dndrpg.event;

/**
 * characterName defeated bossName. Distinct from DefeatEvent (which is about
 * something being defeated) because a boss kill is the session's climactic
 * outcome and gets its own audit-logged record too -- see GameService's
 * AUDIT-1 note: this event is what gets displayed, the audit log entry is
 * the separate, permanent security record. Both are still needed.
 */
public record BossDefeatedEvent(String characterName, String bossName) implements GameEvent {
    public BossDefeatedEvent {
        if (characterName == null || characterName.isBlank()) {
            throw new IllegalArgumentException("characterName must not be blank");
        }
        if (bossName == null || bossName.isBlank()) {
            throw new IllegalArgumentException("bossName must not be blank");
        }
    }
}
