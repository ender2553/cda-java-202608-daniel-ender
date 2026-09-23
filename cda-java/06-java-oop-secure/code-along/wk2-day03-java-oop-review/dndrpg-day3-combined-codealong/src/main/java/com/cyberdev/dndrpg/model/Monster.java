package com.cyberdev.dndrpg.model;

import com.cyberdev.dndrpg.exception.InvalidCharacterStateException;

/**
 * A live, damageable monster instance created from an immutable
 * MonsterTemplate. The template never changes; this object tracks the
 * mutable state of one specific encounter.
 *
 * INSTRUCTOR NOTE (class inheritance): Monster is the base of a small,
 * deliberately narrow inheritance hierarchy. It is NOT sealed and NOT
 * final -- BossMonster (same package) extends it and overrides
 * getEffectiveAttack() to add an "enrage" mechanic once the boss drops
 * below half HP. This is the codebase's one true extends/super/@Override
 * example, contrasted on purpose with the four PlayerCharacter classes
 * (Warrior/Mage/Rogue/Cleric), which differ only in DATA and are
 * correctly modeled as a ClassDefinition record instead of subclasses.
 * Rule of thumb taught here: subclass when BEHAVIOR differs (a boss
 * fights differently once wounded); compose/parameterize when only DATA
 * differs (a mage's stats vs. a warrior's).
 *
 * Callers never construct BossMonster directly -- they call the static
 * factory Monster.of(template), which returns the right concrete type
 * based on template.boss(). This keeps GameService coupled only to the
 * Monster base type while still getting BossMonster's overridden
 * behavior polymorphically (Liskov substitution: every caller that
 * works with a Monster also works, unmodified, with a BossMonster).
 */
public class Monster implements Combatant {
    private final MonsterTemplate template;
    private int currentHp;

    public Monster(MonsterTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("template must not be null");
        }
        this.template = template;
        this.currentHp = template.hp();
    }

    /**
     * Factory that hides the Monster/BossMonster split from callers.
     * GameService calls Monster.of(...) instead of "new Monster(...)"
     * or "new BossMonster(...)" directly, so it never needs an if/else
     * on template.boss() -- the polymorphism lives here, once.
     */
    public static Monster of(MonsterTemplate template) {
        return template.boss() ? new BossMonster(template) : new Monster(template);
    }

    @Override
    public String getDisplayName() {
        return template.name();
    }

    @Override
    public int getHp() {
        return currentHp;
    }

    @Override
    public void takeDamage(int amount) {
        if (amount < 0) {
            // Fail closed: a negative "damage" amount would heal the
            // monster by accident (or by a bug elsewhere in the caller).
            throw new InvalidCharacterStateException("Damage amount must not be negative: " + amount);
        }
        currentHp = Math.max(0, currentHp - amount);
    }

    @Override
    public boolean isDefeated() {
        return currentHp <= 0;
    }

    @Override
    public int getEffectiveAttack() {
        return template.attack();
    }

    @Override
    public int getEffectiveDefense() {
        return template.defense();
    }

    public MonsterTemplate getTemplate() {
        return template;
    }
}
