package com.vitalledger.repository;

import com.vitalledger.domain.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Secure repository for {@link User}. Parameterized access only.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
