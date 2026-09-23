package com.cyberdev.dndrpg.service;

import com.cyberdev.dndrpg.event.AttackEvent;
import com.cyberdev.dndrpg.model.Combatant;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * D20-style combat resolution, deliberately simple: this class exists to
 * demonstrate polymorphism (it never needs to know if a Combatant is a
 * PlayerCharacter or a Monster) rather than to be a complete combat
 * system.
 * <p>
 * REFERENCE IMPLEMENTATION (COMBAT-1 solved).
 * <p>
 * DAY 3 CHANGE: resolveAttack now returns an AttackEvent instead of a
 * formatted String. All of the d20/armor-class/damage-floor logic below is
 * unchanged from Day 1/2 -- only the return type changed, and the method no
 * longer builds any display text itself. Turning a narrated line into a
 * plain data record is exactly what "the sealed hierarchy has been
 * introduced" (see GameService's Day 2 javadoc) unlocks: GameService can
 * now accumulate these into a List&lt;GameEvent&gt;, and ConsoleUI can print
 * them however it likes via an exhaustive switch, instead of this class
 * hard-coding the wording.
 */
@Component
public final class CombatEngine {

    private final Random random;

    public CombatEngine() {
        this(new Random());
    }

    // Package-visible constructor so tests can inject a seeded Random.
    CombatEngine(Random random) {
        this.random = random;

    }

    /**
     * COMBAT-1 solution: resolves one attack from attacker against
     * defender.
     * - Roll a d20, add the attacker's effective attack.
     * - Compare against 10 + defender's effective defense (a simple
     * fixed "armor class" baseline).
     * - On a hit, damage = attacker's effective attack minus the
     * defender's effective defense, floored at 1 so combat always
     * makes forward progress.
     * - Applies the damage via defender.takeDamage(...) -- both
     * PlayerCharacter and Monster enforce their own "never below
     * zero" invariant, so this method doesn't re-check that itself.
     * Common student mistakes: applying damage on a miss; forgetting the
     * Math.max(1, ...) floor (a heavily-armored defender could otherwise
     * take zero or negative damage forever); calling takeDamage with a
     * negative number if attack < defense.
     */
    public AttackEvent resolveAttack(Combatant attacker, Combatant defender) {
        if (attacker == null || defender == null) {
            throw new IllegalArgumentException("attacker and defender must not be null");
        }
        int roll = 1 + random.nextInt(20);
        int attackTotal = roll + attacker.getEffectiveAttack();
        int armorClass = 10 + defender.getEffectiveDefense();

        if (attackTotal < armorClass) {
            return new AttackEvent(attacker.getDisplayName(), defender.getDisplayName(),
                    false, 0, defender.getHp());
        }

        int damage = Math.max(1, attacker.getEffectiveAttack() - defender.getEffectiveDefense());
        defender.takeDamage(damage);
        return new AttackEvent(attacker.getDisplayName(), defender.getDisplayName(),
                true, damage, defender.getHp());
    }
}
