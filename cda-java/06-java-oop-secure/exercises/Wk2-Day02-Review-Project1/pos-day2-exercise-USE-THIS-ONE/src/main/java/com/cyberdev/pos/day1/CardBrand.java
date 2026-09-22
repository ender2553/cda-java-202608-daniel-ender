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

    // INSTRUCTOR NOTE [POS1-4]: This is an allow-list, not a deny-list. We check "does this
    // look like one of the brands we recognize?" and default to UNKNOWN for everything else,
    // rather than trying to deny-list bad patterns (which is never exhaustive). Common
    // mistakes: (1) throwing an exception for an unrecognized number instead of returning
    // UNKNOWN -- that's fail-OPEN in the sense that a crash can take down checkout instead
    // of gracefully declining the brand-specific perks; (2) checking prefixes in the wrong
    // order (e.g. checking "6" before "6011", or checking "3" before "34"/"37") which
    // misclassifies real numbers; (3) not guarding null/blank input before calling
    // startsWith, causing a NullPointerException instead of failing closed to UNKNOWN.
    public static CardBrand fromNumber(String cardNumber) {
        if (cardNumber == null) {
            return UNKNOWN;
        }
        String digits = cardNumber.strip();
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            return UNKNOWN;
        }
        if (digits.startsWith("4")) {
            return VISA;
        }
        if (digits.startsWith("34") || digits.startsWith("37")) {
            return AMEX;
        }
        if (digits.startsWith("6011")) {
            return DISCOVER;
        }
        if (digits.startsWith("5")) {
            return MASTERCARD;
        }
        return UNKNOWN;
    }
}
