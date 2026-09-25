package com.example.rpg.domain;

/**
 * Sealed polymorphic boundary: only the approved combatant hierarchy can participate in combat.
 */
public sealed interface Combatant permits PlayerCharacter, AbstractCombatant {
    String name();

    int health();

    int attackPower();

    default boolean isAlive() {
        return health() > 0;
    }
}
