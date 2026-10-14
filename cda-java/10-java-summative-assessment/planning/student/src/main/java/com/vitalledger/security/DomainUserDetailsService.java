package com.vitalledger.security;

import com.vitalledger.domain.User;
import com.vitalledger.repository.UserRepository;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads users from the encrypted domain store for authentication. Roles become
 * Spring Security authorities ({@code ROLE_CLINICIAN}, etc.). Passwords are never
 * loaded or compared in plaintext — only the stored BCrypt hash is used.
 */
@Service
public class DomainUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO(phase D): load the User by username (generic "Invalid credentials" on miss), map roles to ROLE_* authorities, and build a UserDetails from the stored BCrypt hash (never plaintext).
        throw new UnsupportedOperationException("TODO(phase D)");
    }
}
