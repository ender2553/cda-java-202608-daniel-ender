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
    public AuthService(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    public UserAccount register(String rawUsername, String rawPassword) {
        // TODO 1: normalize and validate the username and password.
        // TODO 2: use encoder.encode(rawPassword); never store the plaintext password.
        // TODO 3: reject duplicate usernames and save the UserAccount.
        throw new UnsupportedOperationException("TODO: implement secure registration");
    }

    public UserAccount login(String rawUsername, String rawPassword) {
        // TODO 4: find the user, call encoder.matches(rawPassword, storedHash),
        // and throw the same generic error for every authentication failure.
        throw new UnsupportedOperationException("TODO: implement secure login");
    }

    private IllegalArgumentException invalidCredentials() { return new IllegalArgumentException("invalid credentials"); }
}
