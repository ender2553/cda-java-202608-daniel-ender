package com.cyberdev.secsuite.service;

import org.springframework.stereotype.Service;

import com.cyberdev.secsuite.exception.AuthenticationException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.repository.AnalystRepository;
import com.cyberdev.secsuite.security.EncryptionService;
import com.cyberdev.secsuite.security.PasswordHasher;

import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Analyst registration and login -- the application's only authentication gate.
 *
 * Every collaborator is constructor-injected: the repository (so InMemory* or Jdbc* can be
 * swapped), the PasswordHasher and EncryptionService (so the crypto primitives stay in the
 * security package and are testable on their own), and a Clock.
 */
@Service
public class AuthService {

    /** Generic, identical message for EVERY login failure -- see SEC-13. */
    public static final String LOGIN_FAILED = "Invalid username or password";

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]{1,64}@[^@\\s]+\\.[^@\\s]{2,}$");

    private final AnalystRepository analystRepository;
    private final PasswordHasher passwordHasher;
    private final EncryptionService encryptionService;
    private final Clock clock;

    /**
     * A real PBKDF2 hash of a random throwaway password, computed once. Login verifies against
     * it when the username does not exist, so "unknown user" costs the same ~100ms of PBKDF2
     * work as "wrong password" -- see SEC-13.
     */
    private final String dummyHash;

    public AuthService(AnalystRepository analystRepository, PasswordHasher passwordHasher,
                       EncryptionService encryptionService, Clock clock) {
        if (analystRepository == null || passwordHasher == null || encryptionService == null || clock == null) {
            throw new ValidationException("AuthService dependencies must not be null");
        }
        this.analystRepository = analystRepository;
        this.passwordHasher = passwordHasher;
        this.encryptionService = encryptionService;
        this.clock = clock;
        this.dummyHash = passwordHasher.hash(UUID.randomUUID().toString().toCharArray());
    }

    // GIVEN/PROVIDED IMPLEMENTATION -- enrollment stores NO secret in
    // plaintext -- the password is HASHED (PasswordHasher.hash, one-way) and the contact email
    // is ENCRYPTED (EncryptionService.encrypt, two-way, because we legitimately need to read
    // it back to contact the analyst). Steps: normalize the username (strip + lower-case with
    // Locale.ROOT) and validate it against Analyst.USERNAME; validate the password (non-null,
    // 12-128 chars -- length, not composition rules, per NIST SP 800-63B); validate the email
    // shape and length; refuse an already-taken username with AuthenticationException; then
    // hash, encrypt, build the Analyst (id 0L, createdAt = clock.instant()), and return the
    // persisted Analyst returned by save().
    // Validation failures throw ValidationException. Common mistakes: (1) storing the raw
    // password or raw email "temporarily"; (2) encrypting the password instead of hashing it
    // (anyone with the key can then recover every password); (3) hashing the email (then the
    // business can never read it back); (4) logging the password or the Analyst's fields
    // while debugging; (5) checking "username taken" AFTER saving.
    // SECURITY CALLOUT: registration necessarily reveals whether a username is taken -- that
    // is an accepted, documented enumeration trade-off for self-service sign-up (real systems
    // mitigate it with rate limiting/CAPTCHA or email-based flows). LOGIN, however, must never
    // reveal it; see login() below.
    public Analyst register(String username, char[] password, String contactEmail) {
        String normalizedUsername = username == null ? null : username.strip().toLowerCase(Locale.ROOT);
        if (normalizedUsername == null || !Analyst.USERNAME.matcher(normalizedUsername).matches()) {
            throw new ValidationException(
                    "username must be 3-32 chars of a-z, 0-9, '.', '_', '-' and start with a letter");
        }
        if (password == null || password.length < MIN_PASSWORD_LENGTH || password.length > MAX_PASSWORD_LENGTH) {
            throw new ValidationException("password must be between " + MIN_PASSWORD_LENGTH + " and "
                    + MAX_PASSWORD_LENGTH + " characters");
        }
        String normalizedEmail = contactEmail == null ? null : contactEmail.strip();
        if (normalizedEmail == null || normalizedEmail.length() > MAX_EMAIL_LENGTH
                || !EMAIL.matcher(normalizedEmail).matches()) {
            throw new ValidationException("contact email is not a valid email address");
        }
        if (analystRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new AuthenticationException("Username is not available");
        }
        String passwordHash = passwordHasher.hash(password);
        String encryptedEmail = encryptionService.encrypt(normalizedEmail);
        Analyst analyst = new Analyst(0L, normalizedUsername, passwordHash, encryptedEmail,
                clock.instant());
        return analystRepository.save(analyst);
    }

    // GIVEN/PROVIDED IMPLEMENTATION -- login fails closed with INDISTINGUISHABLE
    // failures (QuickPay POS3-7 recap). Unknown username, wrong password, null/blank input,
    // a corrupted stored hash, or ANY unexpected runtime failure must all end in the SAME
    // AuthenticationException with the SAME message (LOGIN_FAILED). This version goes one step
    // further than POS3-7: when the username does not exist it still runs a full PBKDF2
    // verification against a dummy hash, so the two failure paths also take about the same
    // TIME -- otherwise "unknown user" returns in ~0ms and "wrong password" in ~100ms, and the
    // stopwatch becomes the enumeration oracle the identical message was meant to prevent.
    // Common mistakes: different messages ("no such analyst" vs "wrong password"); returning
    // null or false instead of throwing (a caller that forgets to check proceeds as logged
    // in); letting a CryptoException/DataAccessException/NullPointerException escape with
    // internal detail; comparing hashes with String.equals.
    // SECURITY CALLOUT: username enumeration turns a password-guessing attack from
    // "guess user AND password" into "confirm user, then guess password" -- it halves the
    // attacker's problem for free.
    public Analyst login(String username, char[] password) {
        try {
            if (username == null || username.isBlank() || password == null || password.length == 0) {
                throw new AuthenticationException(LOGIN_FAILED);
            }
            String normalizedUsername = username.strip().toLowerCase(Locale.ROOT);
            Optional<Analyst> candidate = analystRepository.findByUsername(normalizedUsername);
            String storedHash = candidate.map(Analyst::getPasswordHash).orElse(dummyHash);
            boolean matches = passwordHasher.verify(password, storedHash);
            if (candidate.isEmpty() || !matches) {
                throw new AuthenticationException(LOGIN_FAILED);
            }
            return candidate.get();
        } catch (AuthenticationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AuthenticationException(LOGIN_FAILED);
        }
    }

    /** GIVEN -- decrypts an analyst's contact email (the "two-way" half of the hash-vs-encrypt lesson). */
    public String decryptContactEmail(Analyst analyst) {
        if (analyst == null) {
            throw new ValidationException("analyst must not be null");
        }
        return encryptionService.decrypt(analyst.getEncryptedContactEmail());
    }
}
