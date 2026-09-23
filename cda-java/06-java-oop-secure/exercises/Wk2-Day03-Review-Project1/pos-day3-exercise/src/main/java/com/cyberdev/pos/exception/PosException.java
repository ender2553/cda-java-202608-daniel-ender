package com.cyberdev.pos.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Base RuntimeException for the whole QuickPay POS application. Every custom exception in
 * the series extends this, so callers that want a broad "something in POS-land went wrong"
 * catch can catch PosException once instead of enumerating every subtype.
 *
 * INSTRUCTOR NOTE: this hierarchy is deliberately separate from Day 3's sealed
 * AuthorizationResult (Approved/Declined/Error). They are two different teaching points for
 * two different situations:
 *   - AuthorizationResult (sealed, Day 3) models an EXPECTED business outcome with exactly
 *     three closed cases. A declined card is not a bug or an error condition -- it is a
 *     normal, anticipated result of calling authorize(), so it is returned as data, not
 *     thrown. Returning it as data also lets the compiler enforce exhaustive handling via
 *     sealed + switch.
 *   - PosException and its subtypes (this package) model genuinely EXCEPTIONAL conditions:
 *     malformed input that should never happen if the caller is well-formed (a negative
 *     quantity, a non-digit last4, a duplicate transaction id slipping past a check, a
 *     simulated-persistence failure). These are thrown because there is no sensible "data"
 *     value to return in their place, and because the caller almost certainly wants the
 *     failure to propagate loudly rather than be silently absorbed into a result object.
 * A good discussion prompt for students: "why does authorization return a sealed result but
 * a malformed card number throws?" -- the answer is about which failures are part of the
 * normal business vocabulary (decline) versus which are defects/edge cases (bad data).
 */
public class PosException extends RuntimeException {

    public PosException(String message) {
        super(message);
    }

    public PosException(String message, Throwable cause) {
        super(message, cause);
    }
}
