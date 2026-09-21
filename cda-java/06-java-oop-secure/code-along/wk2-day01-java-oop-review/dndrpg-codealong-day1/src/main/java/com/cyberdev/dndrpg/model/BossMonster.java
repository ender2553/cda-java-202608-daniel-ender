package com.cyberdev.dndrpg.model;

/**
 * A boss-tier monster that fights differently once it's wounded, not just
 * one with bigger numbers. This is the genuine class-inheritance
 * example: BossMonster extends Monster, calls super() from its
 * constructor, and overrides getEffectiveAttack() with @Override, adding
 * an "enrage" bonus once HP drops to half or below.
 *
 */
public class BossMonster extends Monster {

    /**
     * Once HP is at or below this fraction of max HP, the boss enrages
     * and hits harder.
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
