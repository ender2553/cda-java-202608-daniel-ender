package com.cyberdev.dndrpg.model;

import com.cyberdev.dndrpg.exception.InvalidCharacterStateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class PlayerCharacter implements Combatant {
    private final UUID id;
    private final UUID playerId;
    private final String name;
    private final CharacterClass characterClass;
    private final int baseAttack;
    private final int baseDefense;
    private final int maxHp;

    private int currentHp;
    private int level;
    private int xp;
    private int gold;
    private final List<ItemTemplate> inventory; // private, never handed out raw

    public PlayerCharacter(UUID id, UUID playerId, String name, ClassDefinition classDefinition) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (playerId == null) throw new IllegalArgumentException("playerId must not be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (classDefinition == null) throw new IllegalArgumentException("classDefinition must not be null");

        this.id = id;
        this.playerId = playerId;
        this.name = name;
        this.characterClass = classDefinition.characterClass();
        this.baseAttack = classDefinition.baseAttack();
        this.baseDefense = classDefinition.baseDefense();
        this.maxHp = classDefinition.baseHp();
        this.currentHp = maxHp;
        this.level = 1;
        this.xp = 0;
        this.gold = 0;
        this.inventory = new ArrayList<>();
    }

    /**
     * Rebuilds a PlayerCharacter from previously-persisted state. Unlike
     * the public constructor above -- which always starts a brand new
     * character at level 1, full HP, zero xp/gold -- this restores
     * exactly the state a repository read back from the database.
     * Intended to be called from repository-layer code only (see
     * PlayerCharacterRowMapper), never from game logic.
     *
     * Still validate-then-construct: a corrupted row in the database
     * can't produce an invalid in-memory PlayerCharacter either.
     */
    public static PlayerCharacter reconstitute(UUID id, UUID playerId, String name, ClassDefinition classDefinition,
                                                int level, int xp, int gold, int currentHp) {
        if (level < 1) {
            throw new InvalidCharacterStateException("Persisted level must be at least 1: " + level);
        }
        if (xp < 0) {
            throw new InvalidCharacterStateException("Persisted xp must not be negative: " + xp);
        }
        if (gold < 0) {
            throw new InvalidCharacterStateException("Persisted gold must not be negative: " + gold);
        }
        if (classDefinition == null) {
            throw new IllegalArgumentException("classDefinition must not be null");
        }
        if (currentHp < 0 || currentHp > classDefinition.baseHp()) {
            throw new InvalidCharacterStateException("Persisted currentHp out of range: " + currentHp);
        }
        PlayerCharacter character = new PlayerCharacter(id, playerId, name, classDefinition);
        character.level = level;
        character.xp = xp;
        character.gold = gold;
        character.currentHp = currentHp;
        return character;
    }

    // ---- Combatant ----
    @Override
    public String getDisplayName() { return name + " the " + characterClass; }

    @Override
    public int getHp() { return currentHp; }


    @Override
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new InvalidCharacterStateException("Damage amount must not be negative: " + amount);
        }
        currentHp = Math.max(0, currentHp - amount);
    }

    @Override
    public boolean isDefeated() { return currentHp <= 0; }

    @Override
    public int getEffectiveAttack() {
        return baseAttack + inventory.stream().mapToInt(ItemTemplate::attackBonus).sum();
    }

    @Override
    public int getEffectiveDefense() {
        return baseDefense + inventory.stream().mapToInt(ItemTemplate::defenseBonus).sum();
    }

    // ---- gameplay mutators: every one fails closed on bad input ----
    public void heal(int amount) {
        if (amount < 0) {
            throw new InvalidCharacterStateException("Heal amount must not be negative: " + amount);
        }
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public void gainXp(int amount) {
        if (amount < 0) {
            throw new InvalidCharacterStateException("XP amount must not be negative: " + amount);
        }
        xp += amount;
        // Simple level curve: every 100 xp is another level, and leveling
        // up restores the character to full health.
        int newLevel = 1 + (xp / 100);
        if (newLevel > level) {
            level = newLevel;
            currentHp = maxHp;
        }
    }

    public void addGold(int amount) {
        if (amount < 0) {
            throw new InvalidCharacterStateException("Gold amount must not be negative: " + amount);
        }
        gold += amount;
    }

    public void addItem(ItemTemplate item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        inventory.add(item);
    }

    /**
     * Returns a defensive copy of the inventory. Callers get a snapshot
     * they can read and iterate, but mutating the returned list has zero
     * effect on this character's real inventory -- the same pattern as
     * the Day 1 Driver fix, applied with a fresh copy each call because
     * the inventory keeps growing after this method returns.
     */
    public List<ItemTemplate> getInventory() {

        return Collections.unmodifiableList(new ArrayList<>(inventory));
    }

    public UUID getId() { return id; }
    public UUID getPlayerId() { return playerId; }
    public String getName() { return name; }
    public CharacterClass getCharacterClass() { return characterClass; }
    public int getMaxHp() { return maxHp; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getGold() { return gold; }

    /** Score used for the leaderboard: a simple, transparent formula. */
    public int getScore() {
        return (level * 100) + (gold * 2) + xp;
    }

    // IDENTITY EQUALITY, by id: a PlayerCharacter is a mutable entity --
    // level, xp, gold and currentHp all change over its lifetime (see the
    // mutators above) -- so equality can't be based on those fields the
    // way it can for an immutable value like ClassDefinition. Two
    // PlayerCharacter objects are "the same character" if and only if
    // they carry the same id, regardless of what their stats currently
    // are; that's what lets this type live safely in a HashSet/HashMap
    // key (e.g. "the set of characters still in the fight") across a
    // sequence of mutations without silently duplicating an entry.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerCharacter other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
