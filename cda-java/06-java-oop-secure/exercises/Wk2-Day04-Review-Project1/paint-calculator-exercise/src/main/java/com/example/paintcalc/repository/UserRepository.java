package com.example.paintcalc.repository;

import com.example.paintcalc.domain.UserAccount;
import java.util.Optional;

public interface UserRepository {
    UserAccount save(UserAccount user);
    Optional<UserAccount> findByUsername(String username);
}
