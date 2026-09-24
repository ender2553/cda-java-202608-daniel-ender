package com.example.paintcalc.domain;

public record UserAccount(long id, String username, String passwordHash) {
    public UserAccount {
        if (id < 0) throw new IllegalArgumentException("id cannot be negative");
        username = DomainValidation.requiredName(username, "username", 32).toLowerCase();
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("password hash required");
    }
}
