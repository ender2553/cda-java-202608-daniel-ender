package com.example.paintcalc.service;

import com.example.paintcalc.domain.DomainValidation;
import com.example.paintcalc.domain.UserAccount;
import com.example.paintcalc.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public final class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public UserAccount register(String rawUsername, String rawPassword) {
        // TODO 1: normalize and validate the username and password.
        if (rawUsername == null || rawPassword == null) {
            throw new IllegalArgumentException("username and password are required");
        }

        String username = rawUsername.trim().toLowerCase(Locale.ROOT);

        username = DomainValidation.requiredName(username, "username", 32);

        if (rawPassword.isBlank()) {
            throw new IllegalArgumentException("password is required");
        }

        // TODO 2: use encoder.encode(rawPassword); never store the plaintext password.
        String passwordHash = encoder.encode(rawPassword);

        // TODO 3: reject duplicate usernames and save the UserAccount.
        if (users.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("username already exists");
        }

        UserAccount user = new UserAccount(0, username, passwordHash);

        return users.save(user);
    }

    public UserAccount login(String rawUsername, String rawPassword) {
        // TODO 4: find the user, call encoder.matches(rawPassword, storedHash),
        // and throw the same generic error for every authentication failure.
        if (rawUsername == null || rawPassword == null) {
            throw invalidCredentials();
        }

        String username = rawUsername.trim().toLowerCase(Locale.ROOT);

        if (username.isBlank()) {
            throw invalidCredentials();
        }

        UserAccount user = users.findByUsername(username)
                .orElseThrow(this::invalidCredentials);

        if (!encoder.matches(rawPassword, user.passwordHash())) {
            throw invalidCredentials();
        }

        return user;
    }

    private IllegalArgumentException invalidCredentials() {
        return new IllegalArgumentException("invalid credentials");
    }
}

