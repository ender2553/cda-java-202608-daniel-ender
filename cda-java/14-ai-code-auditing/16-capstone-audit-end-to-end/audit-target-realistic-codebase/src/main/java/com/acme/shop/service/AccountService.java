package com.acme.shop.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final PasswordEncoder passwordEncoder;

    public AccountService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public boolean verifyLogin(String rawPassword, String storedHash) {
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}