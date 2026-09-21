package com.cyberdev.dndrpg.model;

import java.util.Objects;

/**
 * Immutable reference data describing one kind of monster. A plain class
 * with private final fields, a validating constructor, and accessor
 * methods -- written out by hand instead of using a Java record, so the
 * "no setters, only a constructor and accessors" shape is visible rather
 * than generated.
 */
public class MonsterTemplate {
    private final String id;
    private final String name;
    private final int hp;
    private final int attack;
    private final int defense;
    private final int xpReward;
    private final int goldReward;
    private final boolean boss;

    public MonsterTemplate(String id, String name, int hp, int attack, int defense,
                            int xpReward, int goldReward, boolean boss) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (hp <= 0) throw new IllegalArgumentException("hp must be positive");
        if (attack < 0) throw new IllegalArgumentException("attack must not be negative");
        if (defense < 0) throw new IllegalArgumentException("defense must not be negative");
        if (xpReward < 0) throw new IllegalArgumentException("xpReward must not be negative");
        if (goldReward < 0) throw new IllegalArgumentException("goldReward must not be negative");
        this.id = id;
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.xpReward = xpReward;
        this.goldReward = goldReward;
        this.boss = boss;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int hp() {
        return hp;
    }

    public int attack() {
        return attack;
    }

    public int defense() {
        return defense;
    }

    public int xpReward() {
        return xpReward;
    }

    public int goldReward() {
        return goldReward;
    }

    public boolean boss() {
        return boss;
    }

    // IDENTITY EQUALITY, by the natural key "id": monster_template.id is
    // the primary key in schema.sql (e.g. "goblin", "dragon"), and it is
    // never blank by construction, so it is a safe, stable identity --
    // two MonsterTemplate objects with id "goblin" describe the same kind
    // of monster even if their hp/attack numbers were tuned differently
    // between two loads (which would itself be a data bug worth catching
    // with a test that puts two such templates in a Set and expects one).
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonsterTemplate other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
