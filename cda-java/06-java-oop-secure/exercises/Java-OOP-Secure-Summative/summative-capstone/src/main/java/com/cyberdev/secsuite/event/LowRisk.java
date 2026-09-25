package com.cyberdev.secsuite.event;

import com.cyberdev.secsuite.exception.ValidationException;

// INSTRUCTOR NOTE [SEC-5]: One leaf of the sealed RiskAssessment hierarchy. A record that
// implements a sealed interface is implicitly final, which is exactly what a closed permits
// list needs. The compact constructor re-checks that the score really belongs to THIS band
// (1-6), so a mis-banded instance (e.g. new LowRisk(25)) cannot exist -- the same "an invalid
// object can never be constructed" discipline as SEC-1. Common mistake: forgetting
// `implements RiskAssessment` (compile error on the permits list -- worth letting students
// hit once) or making these mutable classes instead of records.
public record LowRisk(int score) implements RiskAssessment {

    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 6;

    public LowRisk {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new ValidationException(
                    "LowRisk score must be between " + MIN_SCORE + " and " + MAX_SCORE);
        }
    }
}
