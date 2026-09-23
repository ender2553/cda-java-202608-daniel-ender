package com.cyberdev.pos.day3;

import com.cyberdev.pos.exception.ValidationException;

// NOTE [POS3-2]: see Approved.java for the general sealed-record concept.
// Declined carries only a human-readable reason -- no amount, no auth code -- which is
// the point of modeling each outcome as its own type instead of one grab-bag class with
// nullable fields for "whichever ones apply."
public record Declined(String reason) implements AuthorizationResult {
    // TODO [POS3-2]: Validate reason (non-null, non-blank), throwing ValidationException on
    // failure.
    public Declined {
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("reason must not be null or blank");
        }
    }
}
