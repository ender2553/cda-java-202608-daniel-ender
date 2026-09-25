package com.example.rpg.domain;

import java.util.*;

/** Small domain-level validation helpers. Keeping validation near the model prevents invalid state. */
public final class Domain {
    private Domain() { }
    public static String cleanName(String value, String field, int max) {
        Objects.requireNonNull(value, field + " is required");
        String clean = value.trim();
        // Allow-list input instead of trying to blacklist every unsafe character.
        if (!clean.matches("[A-Za-z0-9][A-Za-z0-9 _-]{1," + (max - 1) + "}"))
            throw new IllegalArgumentException(field + " contains invalid characters or length");
        return clean;
    }
    public static int positive(int value, String field) { if (value < 0) throw new IllegalArgumentException(field + " cannot be negative"); return value; }
}
