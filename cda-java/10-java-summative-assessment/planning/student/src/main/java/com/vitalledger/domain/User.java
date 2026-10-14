package com.vitalledger.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * An application user. The stored {@code passwordHash} is a BCrypt hash — never a
 * plaintext password. Roles are exposed only as an unmodifiable defensive copy so
 * callers cannot mutate a user's authorization set through a leaked reference.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** BCrypt hash only. */
    @Column(nullable = false, length = 100)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    protected User() {
        // JPA
    }

    public User(String username, String passwordHash, Set<Role> roles) {
        this.username = username;
        this.passwordHash = passwordHash;
        // Defensive copy in: caller's set cannot become our internal state.
        this.roles = (roles == null || roles.isEmpty())
                ? EnumSet.noneOf(Role.class)
                : EnumSet.copyOf(roles);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /** Defensive copy out: the internal role set is never leaked or mutable. */
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(EnumSet.copyOf(
                roles.isEmpty() ? EnumSet.noneOf(Role.class) : roles));
    }
}
