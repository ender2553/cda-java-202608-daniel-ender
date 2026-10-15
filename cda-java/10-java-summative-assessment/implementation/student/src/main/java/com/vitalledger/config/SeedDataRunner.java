package com.vitalledger.config;

import com.vitalledger.domain.Role;
import com.vitalledger.domain.User;
import com.vitalledger.repository.UserRepository;

import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds demo users once, at startup, if none exist. Passwords are BCrypt-hashed
 * here (never stored in plaintext). These are clearly-labeled <b>demo</b> credentials
 * for a reference app — see README — not production secrets, and not key material.
 *
 * <p>Runs as an {@link ApplicationRunner} (after context init), so the AES cipher is
 * already wired and any encrypted seed data would be written as ciphertext.</p>
 */
@Component
public class SeedDataRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        seed("clinician", "Clinician!Demo1", Set.of(Role.CLINICIAN));
        seed("auditor", "Auditor!Demo1", Set.of(Role.AUDITOR));
        seed("admin", "Admin!Demo1", Set.of(Role.ADMIN));
    }

    private void seed(String username, String rawPassword, Set<Role> roles) {
        userRepository.save(new User(username, passwordEncoder.encode(rawPassword), roles));
    }
}
