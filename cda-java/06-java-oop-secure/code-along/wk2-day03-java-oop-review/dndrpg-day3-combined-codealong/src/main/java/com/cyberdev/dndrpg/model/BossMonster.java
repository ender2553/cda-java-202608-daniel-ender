package com.cyberdev.dndrpg.model;

/**
 * A boss-tier monster that fights differently once it's wounded, not just
 * one with bigger numbers. This is the exercise's genuine class-inheritance
 * example: BossMonster extends Monster, calls super() from its
 * constructor, and overrides getEffectiveAttack() with @Override, adding
 * an "enrage" bonus once HP drops to half or below.
 *
 * INSTRUCTOR NOTE: contrast this with why the four PlayerCharacter classes
 * (Warrior/Mage/Rogue/Cleric) do NOT do this. Those four differ only in
 * starting stats -- pure data -- so they are correctly modeled with a
 * single PlayerCharacter class parameterized by a ClassDefinition record.
 * Subclassing them ("class WarriorCharacter extends PlayerCharacter")
 * would be the classic "type code as subclass" anti-pattern: four classes
 * whose only difference is which numbers a constructor was called with,
 * with no distinct behavior to justify a distinct type. BossMonster earns
 * its subclass because it has behavior the base type does not: an enrage
 * threshold that changes combat math mid-fight. That is the line the
 * curriculum draws between "subclass this" and "just add a field."
 *
 * Constructed only via Monster.of(template) -- never call "new
 * BossMonster(...)" from outside the model package; that would bypass the
 * factory and re-introduce the branching logic Monster.of() exists to
 * centralize.
 */
public class BossMonster extends Monster {

    /**
     * Once HP is at or below this fraction of max HP, the boss enrages
     * and hits harder. A simple, visible mechanic so students can watch
     * polymorphism change the actual combat log, not just read about it.
     */
    private static final double ENRAGE_HP_THRESHOLD = 0.5;
    private static final int ENRAGE_ATTACK_BONUS = 5;

    public BossMonster(MonsterTemplate template) {
        super(template);
    }

    /**
     * Overrides the base implementation: starts from the same attack
     * value Monster would return (via super.getEffectiveAttack()), then
     * adds a bonus while enraged. CombatEngine and GameService never
     * check "is this a BossMonster" -- they call getEffectiveAttack() on
     * whatever Combatant/Monster reference they're holding and this
     * override just... happens, which is the point of polymorphism.
     */
    @Override
    public int getEffectiveAttack() {
        int base = super.getEffectiveAttack();
        if (isEnraged()) {
            return base + ENRAGE_ATTACK_BONUS;
        }
        return base;
    }

    private boolean isEnraged() {
        return getHp() <= getTemplate().hp() * ENRAGE_HP_THRESHOLD;
    }
}
