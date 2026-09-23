package com.cyberdev.pos.day3;

import com.cyberdev.pos.exception.ValidationException;

// NOTE [POS3-2]: Error is a distinct case from Declined -- Declined means "the
// processor looked at this and said no" (a business outcome), Error means "something
// about the request or the processor itself was wrong" (an operational outcome). Keeping
// them separate lets CheckoutService's switch react differently later (e.g. retry on
// Error, never retry on Declined) without stringly-typed reason codes.
public record Error(String message) implements AuthorizationResult {
    // TODO [POS3-2]: Validate message (non-null, non-blank), throwing ValidationException on
    // failure.
    public Error {
        if (message == null || message.isBlank()) {
            throw new ValidationException("message must not be null or blank");
        }
    }
}
