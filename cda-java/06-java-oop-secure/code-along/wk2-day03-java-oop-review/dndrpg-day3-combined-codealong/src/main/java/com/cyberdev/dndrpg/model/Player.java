package com.cyberdev.dndrpg.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A player's account. Note what is deliberately NOT here: no plaintext
 * password, anywhere, ever. passwordHash is already-hashed output from
 * PasswordHasher by the time it reaches this constructor -- this class
 * has no idea what algorithm produced it, and that's the point.
 *
 * INSTRUCTOR NOTE (Day 2 scope): this class -- and the JdbcPlayerRepository
 * / PlayerRowMapper that persist it -- are introduced today purely as
 * repository-pattern material: an account row, a primary key, a
 * RowMapper. Nothing in today's ConsoleUI actually creates a Player yet
 * (there's still no login/registration flow); how passwordHash and
 * encryptedEmail get produced -- PasswordHasher, EncryptionService,
 * AuthService -- is Day 4 material.
 */
public final class Player {
    private final UUID id;
    private final String username;
    private final String passwordHash;       // PBKDF2 hash, never the raw password
    private final String encryptedEmail;     // AES-256-GCM ciphertext, Base64-encoded
    private final Instant createdAt;

    public Player(UUID id, String username, String passwordHash, String encryptedEmail, Instant createdAt) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        if (encryptedEmail == null || encryptedEmail.isBlank()) {
            throw new IllegalArgumentException("encryptedEmail must not be blank");
        }
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.encryptedEmail = encryptedEmail;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEncryptedEmail() { return encryptedEmail; }
    public Instant getCreatedAt() { return createdAt; }

    // IDENTITY EQUALITY, by id: a Player is an account record -- its
    // username, hash and encrypted email can all change over the
    // account's lifetime (a rename, a password reset) without it
    // becoming a "different" player. Only the surrogate UUID id, handed
    // out once at registration and never reused, identifies "this
    // account" for things like a HashSet of logged-in players.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        // SECURITY: never include passwordHash or encryptedEmail in a
        // toString() that might land in a log line -- even "just the
        // hash" is more than a log line needs, and it trains reviewers
        // to stop noticing when a real secret shows up here later.
        return "Player{id=" + id + ", username='" + username + "'}";
    }
}
