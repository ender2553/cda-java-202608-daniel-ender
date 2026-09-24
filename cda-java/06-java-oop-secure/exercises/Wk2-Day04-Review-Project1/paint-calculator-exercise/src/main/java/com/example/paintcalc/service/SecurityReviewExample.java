package com.example.paintcalc.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.math.BigDecimal;
import java.util.List;

/** SECURITY REPAIR LAB: this class intentionally contains insecure code. Fix it before submission. */
public final class SecurityReviewExample {
    private SecurityReviewExample() { }

    public static String hashPassword(String rawPassword) {
        // TODO 9: replace this plaintext return with BCrypt PasswordEncoder.encode(...).
        return new BCryptPasswordEncoder().encode(rawPassword);
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        // TODO 10: use PasswordEncoder.matches(...); never compare plaintext passwords.
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        return new BCryptPasswordEncoder().matches(rawPassword, storedHash);
    }

    public static String safeUserQuery() {
        // TODO 11: return SQL with a ? placeholder. Do not concatenate username input into SQL.
        return "select id from paint_user where username=?";
    }

    public static List<String> safeInventoryCopy(List<String> inventory) {
        // TODO 12: return an immutable defensive copy, not the mutable list itself.
        return List.copyOf(inventory);
    }

    public static BigDecimal safeCost(BigDecimal unitPrice, int gallons) {
        // TODO 13: use BigDecimal arithmetic and setScale(2); avoid double for money.
        return unitPrice
                .multiply(BigDecimal.valueOf(gallons))
                .setScale(2);
    }
}

