package com.cyberdev.dndrpg.event;

/**
 * One resolved attack: attackerName attacked defenderName, either hitting
 * for damage or missing. Emitted by CombatEngine.resolveAttack in place of
 * the formatted String it used to build directly (see CombatEngine's Day 3
 * update).
 */
public record AttackEvent(String attackerName, String defenderName, boolean hit,
                           int damage, int defenderHpRemaining) implements GameEvent {
    public AttackEvent {
        if (attackerName == null || attackerName.isBlank()) {
            throw new IllegalArgumentException("attackerName must not be blank");
        }
        if (defenderName == null || defenderName.isBlank()) {
            throw new IllegalArgumentException("defenderName must not be blank");
        }
        if (damage < 0) {
            throw new IllegalArgumentException("damage must not be negative");
        }
    }
}
