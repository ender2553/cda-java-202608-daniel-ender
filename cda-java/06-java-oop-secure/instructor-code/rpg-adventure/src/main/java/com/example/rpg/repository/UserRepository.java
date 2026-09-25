package com.example.rpg.repository;
import com.example.rpg.domain.UserAccount;
import java.util.Optional;
/** Repository abstraction: services do not know whether data is in memory, a file, or SQL. */
public interface UserRepository { UserAccount save(UserAccount user); Optional<UserAccount> findByUsername(String username); }
