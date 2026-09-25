package com.example.rpg.domain;

import java.util.Objects;

/** Immutable value object. Records provide final state and value-based equality by default. */
public record Item(String id, String name, ItemType type, Rarity rarity, int power, int value) {
    public Item {
        id = Domain.cleanName(id, "item id", 40); name = Domain.cleanName(name, "item name", 40);
        Objects.requireNonNull(type); Objects.requireNonNull(rarity);
        if (power < 0 || value < 0) throw new IllegalArgumentException("power/value cannot be negative");
    }
    public boolean isEquippable() { return type == ItemType.WEAPON || type == ItemType.ARMOR; }
}
