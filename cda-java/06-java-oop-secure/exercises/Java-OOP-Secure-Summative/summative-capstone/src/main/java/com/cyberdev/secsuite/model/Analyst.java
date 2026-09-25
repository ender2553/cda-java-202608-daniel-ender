package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * A security analyst account (maps to "analyst"). Holds only the PBKDF2 password hash
 * (SEC-13, self-describing "pbkdf2_sha256$iterations$salt$hash" string, never the password)
 * and the AES-GCM-encrypted contact email (SEC-14, Base64 IV||ciphertext, never the raw
 * address).
 *
 * ENTITY: equal by id alone (see the note on Asset for the reasoning).
 *
 * SECURITY CALLOUT: toString() is overridden to REDACT the hash and the encrypted email. The
 * default record/IDE-generated toString would print both straight into any log line or
 * exception message that happened to include an Analyst -- secrets end up in logs far more
 * often through an innocent toString() than through a deliberate print statement.
 */
public final class Analyst {

    /** Lower-case letters, digits, dot, underscore, hyphen; 3-32 chars; must start with a letter. */
    public static final Pattern USERNAME = Pattern.compile("^[a-z][a-z0-9._-]{2,31}$");

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final String encryptedContactEmail;
    private final Instant createdAt;

    public Analyst(Long id, String username, String passwordHash, String encryptedContactEmail, Instant createdAt) {
        if (id == null) {
            throw new ValidationException("analyst id must not be null");
        }
        if (username == null || !USERNAME.matcher(username).matches()) {
            throw new ValidationException(
                    "username must be 3-32 chars of a-z, 0-9, '.', '_', '-' and start with a letter");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("passwordHash must not be blank");
        }
        if (encryptedContactEmail == null || encryptedContactEmail.isBlank()) {
            throw new ValidationException("encryptedContactEmail must not be blank");
        }
        if (createdAt == null) {
            throw new ValidationException("createdAt must not be null");
        }
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.encryptedContactEmail = encryptedContactEmail;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Analyst withId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive");
        }
        return new Analyst(id, username, passwordHash, encryptedContactEmail, createdAt);
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEncryptedContactEmail() {
        return encryptedContactEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Analyst other)) return false;
        return id != null && id > 0 && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Analyst{username=" + username + ", passwordHash=[REDACTED], contactEmail=[ENCRYPTED]}";
    }
}
