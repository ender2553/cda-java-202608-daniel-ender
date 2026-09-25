package com.cyberdev.secsuite.event;

import com.cyberdev.secsuite.exception.ValidationException;

// INSTRUCTOR NOTE [SEC-5]: One leaf of the sealed RiskAssessment hierarchy. A record that
// implements a sealed interface is implicitly final, which is exactly what a closed permits
// list needs. The compact constructor re-checks that the score really belongs to THIS band
// (20-25), so a mis-banded instance (e.g. new CriticalRisk(3)) cannot exist -- the same "an invalid
// object can never be constructed" discipline as SEC-1. Common mistake: forgetting
// `implements RiskAssessment` (compile error on the permits list -- worth letting students
// hit once) or making these mutable classes instead of records.
public record CriticalRisk(int score) implements RiskAssessment {

    public static final int MIN_SCORE = 20;
    public static final int MAX_SCORE = 25;

    public CriticalRisk {
        throw new UnsupportedOperationException(
                "TODO [SEC-5]: reject a score outside this band (MIN_SCORE..MAX_SCORE = 20..25) with ValidationException");
    }
}
