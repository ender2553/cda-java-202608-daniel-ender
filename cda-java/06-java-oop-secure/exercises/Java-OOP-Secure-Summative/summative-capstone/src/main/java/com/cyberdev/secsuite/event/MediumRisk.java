package com.cyberdev.secsuite.event;

import com.cyberdev.secsuite.exception.ValidationException;

// INSTRUCTOR NOTE [SEC-5]: One leaf of the sealed RiskAssessment hierarchy. A record that
// implements a sealed interface is implicitly final, which is exactly what a closed permits
// list needs. The compact constructor re-checks that the score really belongs to THIS band
// (7-12), so a mis-banded instance (e.g. new MediumRisk(3)) cannot exist -- the same "an invalid
// object can never be constructed" discipline as SEC-1. Common mistake: forgetting
// `implements RiskAssessment` (compile error on the permits list -- worth letting students
// hit once) or making these mutable classes instead of records.
public record MediumRisk(int score) implements RiskAssessment {

    public static final int MIN_SCORE = 7;
    public static final int MAX_SCORE = 12;

    public MediumRisk {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new ValidationException(
                    "MediumRisk score must be between " + MIN_SCORE + " and " + MAX_SCORE);
        }
    }
}
