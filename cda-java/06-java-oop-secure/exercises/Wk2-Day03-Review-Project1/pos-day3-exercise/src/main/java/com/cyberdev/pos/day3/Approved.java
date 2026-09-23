package com.cyberdev.pos.day3;

import com.cyberdev.pos.exception.ValidationException;

// INSTRUCTOR NOTE [POS3-2]: A record implementing a sealed interface is implicitly final
// (or, for records, non-extendable by definition), which is exactly what the sealed
// hierarchy needs -- the permits list in AuthorizationResult names concrete, closed
// leaves. Concept tested: sealed interface + record combo as an exhaustive, type-safe
// "outcome enum with data" -- richer than a plain enum because each case carries a
// different payload (authCode+amount vs a reason string vs a message). Common mistake:
// making these classes instead of records (then forgetting equals/hashCode, or leaving
// fields mutable) or forgetting `implements AuthorizationResult`, which breaks the sealed
// permits list and fails compilation with a clear error -- a good thing to let students
// discover once.
public record Approved(String authCode, Money amount) implements AuthorizationResult {
    // TODO [POS3-2]: Validate authCode (non-null, non-blank) and amount (non-null), throwing
    // ValidationException on failure.
    public Approved {
        if (authCode == null || authCode.isBlank()) {
            throw new ValidationException("authCode must not be null or blank");
        }

        if (amount == null) {
            throw new ValidationException("amount must not be null");
        }
    }
}
