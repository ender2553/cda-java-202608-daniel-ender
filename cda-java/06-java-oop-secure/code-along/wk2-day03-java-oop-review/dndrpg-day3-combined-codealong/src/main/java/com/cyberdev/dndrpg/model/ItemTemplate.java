package com.cyberdev.dndrpg.model;

import java.util.Objects;

/**
 * Immutable reference data describing one kind of item. A plain class
 * with private final fields, a validating constructor, and accessor
 * methods -- the same shape a Java record would generate for us, written
 * out here so every part of it is visible.
 */
public class ItemTemplate {
    private final String id;
    private final String name;
    private final ItemType type;
    private final int attackBonus;
    private final int defenseBonus;
    private final int value;

    public ItemTemplate(String id, String name, ItemType type, int attackBonus,
                         int defenseBonus, int value) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (attackBonus < 0) throw new IllegalArgumentException("attackBonus must not be negative");
        if (defenseBonus < 0) throw new IllegalArgumentException("defenseBonus must not be negative");
        if (value < 0) throw new IllegalArgumentException("value must not be negative");
        this.id = id;
        this.name = name;
        this.type = type;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.value = value;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public ItemType type() {
        return type;
    }

    public int attackBonus() {
        return attackBonus;
    }

    public int defenseBonus() {
        return defenseBonus;
    }

    public int value() {
        return value;
    }

    // IDENTITY EQUALITY, by the natural key "id": item_template.id is the
    // primary key in schema.sql, and this class's own constructor never
    // allows a blank id, so id is a safe, always-present identity --
    // "flame_blade" from the CSV loader and "flame_blade" from the JDBC
    // loader are the same item template even though the two Java objects
    // are different instances.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemTemplate other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
