package com.example.paintcalc.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class DomainValidation {
    private DomainValidation() { }
    public static String requiredName(String value, String field, int max) {
        Objects.requireNonNull(value, field + " is required");
        String clean = value.trim();
        // SECURITY: allow-list names instead of trying to blacklist dangerous characters.
        if (!clean.matches("[A-Za-z0-9][A-Za-z0-9 _-]{1," + (max - 1) + "}"))
            throw new IllegalArgumentException(field + " contains invalid characters or length");
        return clean;
    }
    public static BigDecimal positive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) throw new IllegalArgumentException(field + " must be positive");
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
