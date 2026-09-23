package com.cyberdev.dndrpg.security;

import com.cyberdev.dndrpg.exception.AuthenticationException;
import com.cyberdev.dndrpg.logging.AuditLogger;
import com.cyberdev.dndrpg.model.Player;
import com.cyberdev.dndrpg.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.UUID;

/**
 * DAY 4 ADDITION. Registers and logs in players, using the persistence
 * (PlayerRepository, given from Day 2) and the crypto primitives above.
 *
 * This service uses constructor injection, so a test can hand
 * this an InMemoryPlayerRepository (fast, no DB) or a JdbcPlayerRepository
 * (real Postgres) without this class's own code changing at all.
 */
@Service
public final class AuthService {

    private final PlayerRepository playerRepository;
    private final PasswordHasher passwordHasher;
    private final EncryptionService encryptionService;
    private final SecretKey emailEncryptionKey;
    private final AuditLogger auditLogger;

    public AuthService(PlayerRepository playerRepository,
                        PasswordHasher passwordHasher,
                        EncryptionService encryptionService,
    @Qualifier("emailEncryptionKey") SecretKey emailEncryptionKey,
                        AuditLogger auditLogger) {
        if (playerRepository == null) throw new IllegalArgumentException("playerRepository must not be null");
        if (passwordHasher == null) throw new IllegalArgumentException("passwordHasher must not be null");
        if (encryptionService == null) throw new IllegalArgumentException("encryptionService must not be null");
        if (emailEncryptionKey == null) throw new IllegalArgumentException("emailEncryptionKey must not be null");
        if (auditLogger == null) throw new IllegalArgumentException("auditLogger must not be null");
        this.playerRepository = playerRepository;
        this.passwordHasher = passwordHasher;
        this.encryptionService = encryptionService;
        this.emailEncryptionKey = emailEncryptionKey;
        this.auditLogger = auditLogger;
    }

    // NOTE: fail-closed on a duplicate username -- checked BEFORE
    // any write happens, the same before-not-after discipline the rest of
    // this codebase's mutators already use (see PlayerCharacter's gainXp/
    // heal/addGold: validate, THEN mutate). Registering hashes the password
    // (PBKDF2, one-way, never recoverable) and encrypts the email
    // (AES-256-GCM, two-way -- the business legitimately needs to read it
    // back later, e.g. to email the player), then audit-logs
    // PLAYER_REGISTERED with the new player's id as actor. The actor is the
    // id, never the username or email -- see AuditEntry's javadoc ("would
    // this be fine for a teammate to grep through?").
    public Player register(String username, String password, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password must not be empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new AuthenticationException("Username is already taken");
        }

        String passwordHash = passwordHasher.hash(password);
        String encryptedEmail = encryptionService.encrypt(email, emailEncryptionKey);
        Player player = new Player(UUID.randomUUID(), username, passwordHash, encryptedEmail, Instant.now());
        playerRepository.save(player);

        auditLogger.log("PLAYER_REGISTERED", player.getId(), "new player account created");
        return player;
    }

    // NOTE: fail-closed login that does NOT let a caller
    // distinguish "unknown username" from "known username, wrong password"
    // -- both resolve to the exact same AuthenticationException with the
    // exact same generic message. A distinguishable error is a
    // username-enumeration oracle: an attacker could script logins against
    // every username guess and learn which ones exist from the error
    // alone, long before ever guessing a correct password. The audit trail
    // still distinguishes the two cases in its `details` text (useful for
    // an incident investigation later), but that detail is never handed
    // back to the caller, and it never includes the raw username the
    // caller typed -- only whether the account existed, which is not PII.
    public Player login(String username, String password) {
        try {
            if (username == null || username.isBlank() || password == null || password.isEmpty()) {
                auditLogger.log("LOGIN_FAILURE", null, "blank username or password");
                throw new AuthenticationException("Invalid username or password");
            }
            Player player = playerRepository.findByUsername(username).orElse(null);
            if (player == null) {
                auditLogger.log("LOGIN_FAILURE", null, "unknown username");
                throw new AuthenticationException("Invalid username or password");
            }
            if (!passwordHasher.verify(password, player.getPasswordHash())) {
                auditLogger.log("LOGIN_FAILURE", player.getId(), "bad password");
                throw new AuthenticationException("Invalid username or password");
            }
            auditLogger.log("LOGIN_SUCCESS", player.getId(), "login succeeded");
            return player;
        } catch (AuthenticationException e) {
            throw e;
        } catch (RuntimeException e) {
            // Fail closed: any other unexpected failure (crypto provider
            // hiccup, repository failure, ...) still surfaces as a login
            // failure, never a leak of whatever internal exception type
            // actually happened.
            auditLogger.log("LOGIN_FAILURE", null, "unexpected failure during login");
            throw new AuthenticationException("Invalid username or password");
        }
    }
}
