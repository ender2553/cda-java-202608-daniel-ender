package com.cyberdev.dndrpg.model;

/**
 * Anything that can take part in combat. PlayerCharacter and Monster both
 * implement this so CombatEngine can resolve an attack between either
 * combination of the two without knowing (or caring) which concrete type
 * it's holding -- polymorphism doing real work, not just a syntax exercise.
 */
public interface Combatant {
    String getDisplayName();
    int getHp();
    void takeDamage(int amount);
    boolean isDefeated();
    int getEffectiveAttack();
    int getEffectiveDefense();
}
