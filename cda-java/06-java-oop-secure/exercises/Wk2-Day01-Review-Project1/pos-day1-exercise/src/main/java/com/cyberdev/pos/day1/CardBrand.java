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

    // TODO [POS1-4]: Implement ALLOW-LIST prefix matching.
    public static CardBrand fromNumber(String cardNumber) {

        // Fail closed for null or blank input
        if (cardNumber == null || cardNumber.isBlank()) {
            return UNKNOWN;
        }

        // Fail closed if any character is not a digit
        for (int i = 0; i < cardNumber.length(); i++) {
            if (!Character.isDigit(cardNumber.charAt(i))) {
                return UNKNOWN;
            }
        }

        // Allow-list prefix matching
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return AMEX;
        }

        if (cardNumber.startsWith("6011")) {
            return DISCOVER;
        }

        if (cardNumber.startsWith("5")) {
            return MASTERCARD;
        }

        if (cardNumber.startsWith("4")) {
            return VISA;
        }

        return UNKNOWN;
    }
}


