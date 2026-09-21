package com.cyberdev.pos.day1;

/**
 * Card brand allow-list. Brand detection must fail CLOSED: an unrecognized or malformed
 * card number never crashes the register and never gets misclassified -- it becomes UNKNOWN.
 */
public enum CardBrand {
    VISA,
    MASTERCARD,
    AMEX,
    DISCOVER,
    UNKNOWN;

    // TODO [POS1-4]: Implement ALLOW-LIST prefix matching:
    //   starts with "4"        -> VISA
    //   starts with "34"/"37"  -> AMEX
    //   starts with "6011"     -> DISCOVER
    //   starts with "5"        -> MASTERCARD
    //   anything else (including null, blank, or non-digit input) -> UNKNOWN
    // This method must NEVER throw -- always fail closed to UNKNOWN.
    public static CardBrand fromNumber(String cardNumber) {
        throw new UnsupportedOperationException("TODO [POS1-4]: allow-list prefix matching, fail closed to UNKNOWN, never throw");
    }
}
