package com.cyberdev.dndrpg.model;

import java.util.Objects;

/**
 * Immutable reference data describing one playable class's starting stats.
 *
 * This is a plain class, not a Java record. A record would generate the
 * constructor, accessors, equals/hashCode and toString automatically, but
 * we write all of that out by hand here so it's clear exactly what's
 * happening: private final fields, a validating constructor, and simple
 * accessor methods -- no setters, so a caller can never mutate this
 * "template" data after it's loaded.
 */
public class ClassDefinition {
    private final CharacterClass characterClass;
    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final String description;

    // Validate-then-construct: this runs on EVERY construction path (file
    // loader, JDBC loader, tests, ...) -- there is no way to build a
    // ClassDefinition with an invalid stat.
    public ClassDefinition(CharacterClass characterClass, int baseHp, int baseAttack,
                            int baseDefense, String description) {
        if (characterClass == null) {
            throw new IllegalArgumentException("characterClass must not be null");
        }
        if (baseHp <= 0) {
            throw new IllegalArgumentException("baseHp must be positive");
        }
        if (baseAttack < 0) {
            throw new IllegalArgumentException("baseAttack must not be negative");
        }
        if (baseDefense < 0) {
            throw new IllegalArgumentException("baseDefense must not be negative");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        this.characterClass = characterClass;
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.description = description;
    }

    public CharacterClass characterClass() {
        return characterClass;
    }

    public int baseHp() {
        return baseHp;
    }

    public int baseAttack() {
        return baseAttack;
    }

    public int baseDefense() {
        return baseDefense;
    }

    public String description() {
        return description;
    }

    // IDENTITY EQUALITY, by the natural key:
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassDefinition other)) return false;
        return characterClass == other.characterClass;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(characterClass);
    }
}
