package com.cyberdev.secsuite.security;

import com.cyberdev.secsuite.exception.AuthenticationException;
import com.cyberdev.secsuite.exception.CryptoException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.repository.inmemory.InMemoryAnalystRepository;
import com.cyberdev.secsuite.service.AuthService;
import com.cyberdev.secsuite.support.SeededSuite;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN REGRESSION TESTS -- PasswordHasher, EncryptionService and AuthService are provided
 * infrastructure. These tests carry no graded points and must not be edited by students.
 *
 * SEC-13 (PBKDF2 password hashing + fail-closed, non-enumerating login) and SEC-14 (AES-GCM
 * encryption of the contact email). No seed data and no database -- these do NOT depend on
 * SEC-1. AuthService's constructor hashes a dummy password, so these tests also verify that
 * the provided components remain correctly integrated.
 */
public class SecurityTests {

    private static final String PASSWORD = "Correct-Horse-42";

    private static AuthService authService(InMemoryAnalystRepository repository, EncryptionService encryption) {
        return new AuthService(repository, new PasswordHasher(), encryption,
                SeededSuite.empty().clock);
    }

    // ---------------------------------------------------------------
    // Provided: PasswordHasher + AuthService register/login
    // ---------------------------------------------------------------

    @Test
    public void hash_freshSaltAndSelfDescribingFormat() {
        PasswordHasher hasher = new PasswordHasher();
        String first = hasher.hash(PASSWORD.toCharArray());
        String second = hasher.hash(PASSWORD.toCharArray());

        assertNotEquals(first, second, "the same password hashed twice must differ (fresh random salt per hash)");
        for (String stored : List.of(first, second)) {
            String[] parts = stored.split("\\$");
            assertEquals(4, parts.length, "format is pbkdf2_sha256$iterations$salt$hash: " + stored);
            assertEquals("pbkdf2_sha256", parts[0]);
            assertEquals("120000", parts[1], "120,000 PBKDF2 iterations");
            assertEquals(16, Base64.getDecoder().decode(parts[2]).length, "16-byte salt");
            assertEquals(32, Base64.getDecoder().decode(parts[3]).length, "256-bit derived key");
            assertFalse(stored.contains(PASSWORD), "the stored value must not contain the password");
        }
        assertTrue(hasher.verify(PASSWORD.toCharArray(), first));
        assertTrue(hasher.verify(PASSWORD.toCharArray(), second));
    }

    @Test
    public void verify_wrongOrMalformedIsFalse() {
        PasswordHasher hasher = new PasswordHasher();
        String stored = hasher.hash(PASSWORD.toCharArray());
        String saltAndHash = stored.substring(stored.indexOf('$', "pbkdf2_sha256$".length()) + 1);

        assertFalse(hasher.verify("Correct-Horse-43".toCharArray(), stored), "wrong password");
        assertFalse(hasher.verify(new char[0], stored), "empty password");
        assertFalse(hasher.verify(null, stored), "null password");
        List<String> malformed = new ArrayList<>(List.of(
                "garbage",
                "",
                "pbkdf2_sha256$abc$" + saltAndHash,                 // non-numeric iterations
                "pbkdf2_sha256$999999999$" + saltAndHash,           // absurd work factor (CPU DoS)
                "md5$120000$" + saltAndHash,                        // wrong scheme
                "pbkdf2_sha256$120000$%%%$###",                     // not Base64
                stored + "$extra"));                                // wrong number of parts
        malformed.add(null);
        for (String bad : malformed) {
            boolean[] result = new boolean[1];
            assertDoesNotThrow(() -> {
                result[0] = hasher.verify(PASSWORD.toCharArray(), bad);
            }, "verify must not throw for a corrupted stored value: " + bad);
            assertFalse(result[0], "verify must fail closed (false) for: " + bad);
        }
    }

    @Test
    public void register_validatesNormalizesAndHashes() {
        InMemoryAnalystRepository repository = new InMemoryAnalystRepository();
        AuthService auth = authService(repository, new EncryptionService(EncryptionService.generateKey()));

        Analyst alice = auth.register("  Alice ", PASSWORD.toCharArray(), "alice@example.com");
        assertEquals("alice", alice.getUsername(), "username is stripped and lower-cased");
        assertTrue(alice.getId() > 0, "register returns the persisted analyst with its generated id");
        assertTrue(repository.findByUsername("alice").isPresent(), "the analyst must be saved");
        assertTrue(alice.getPasswordHash().startsWith("pbkdf2_sha256$"), "the password is stored as a PBKDF2 hash");
        assertFalse(alice.getPasswordHash().contains(PASSWORD), "never the raw password");
        assertTrue(new PasswordHasher().verify(PASSWORD.toCharArray(), alice.getPasswordHash()));

        assertThrows(AuthenticationException.class,
                () -> auth.register("ALICE", "Another-Pass-99".toCharArray(), "a2@example.com"),
                "a taken username (after normalization) is refused");
        assertThrows(ValidationException.class,
                () -> auth.register("bob", "too-short".toCharArray(), "bob@example.com"),
                "passwords shorter than 12 characters are rejected");
        assertThrows(ValidationException.class,
                () -> auth.register("bob", "x".repeat(129).toCharArray(), "bob@example.com"),
                "passwords longer than 128 characters are rejected");
        assertThrows(ValidationException.class,
                () -> auth.register("1bob", PASSWORD.toCharArray(), "bob@example.com"),
                "usernames must start with a letter");
        assertThrows(ValidationException.class,
                () -> auth.register("bob", PASSWORD.toCharArray(), "not-an-email"),
                "the contact email must look like an email address");
        assertEquals(1, countAnalysts(repository), "rejected registrations must not be saved");
    }

    @Test
    public void login_failuresAreIndistinguishable() {
        InMemoryAnalystRepository repository = new InMemoryAnalystRepository();
        AuthService auth = authService(repository, new EncryptionService(EncryptionService.generateKey()));
        auth.register("alice", PASSWORD.toCharArray(), "alice@example.com");

        Analyst loggedIn = auth.login(" ALICE ", PASSWORD.toCharArray());
        assertEquals("alice", loggedIn.getUsername(), "login normalizes the username the same way register does");

        List<String> messages = new ArrayList<>();
        messages.add(assertThrows(AuthenticationException.class,
                () -> auth.login("alice", "Wrong-Password-1".toCharArray()), "wrong password").getMessage());
        messages.add(assertThrows(AuthenticationException.class,
                () -> auth.login("mallory", "Wrong-Password-1".toCharArray()), "unknown user").getMessage());
        messages.add(assertThrows(AuthenticationException.class,
                () -> auth.login(null, PASSWORD.toCharArray()), "null username").getMessage());
        messages.add(assertThrows(AuthenticationException.class,
                () -> auth.login("  ", PASSWORD.toCharArray()), "blank username").getMessage());
        messages.add(assertThrows(AuthenticationException.class,
                () -> auth.login("alice", null), "null password").getMessage());
        messages.add(assertThrows(AuthenticationException.class,
                () -> auth.login("alice", new char[0]), "empty password").getMessage());
        for (String message : messages) {
            assertEquals(AuthService.LOGIN_FAILED, message,
                    "every login failure must carry the identical generic message (no username enumeration)");
        }
    }

    // ---------------------------------------------------------------
    // Provided: EncryptionService.encrypt (+ its use in register)
    // ---------------------------------------------------------------

    @Test
    public void encrypt_freshIvAndRoundTrip() {
        EncryptionService encryption = new EncryptionService(EncryptionService.generateKey());
        String email = "alice@example.com";

        String first = encryption.encrypt(email);
        String second = encryption.encrypt(email);

        assertNotEquals(first, second, "the same plaintext must encrypt differently every time (fresh random IV)");
        assertFalse(first.contains("alice"), "the output must not contain the plaintext");
        byte[] packaged = Base64.getDecoder().decode(first);
        assertEquals(12 + email.getBytes(StandardCharsets.UTF_8).length + 16, packaged.length,
                "layout is Base64( 12-byte IV || ciphertext || 16-byte GCM tag )");
        byte[] iv1 = java.util.Arrays.copyOf(packaged, 12);
        byte[] iv2 = java.util.Arrays.copyOf(Base64.getDecoder().decode(second), 12);
        assertFalse(java.util.Arrays.equals(iv1, iv2), "the IV itself must differ between calls");
        assertEquals(email, encryption.decrypt(first));
        assertEquals(email, encryption.decrypt(second));
        assertEquals("", encryption.decrypt(encryption.encrypt("")), "an empty string round-trips too");
    }

    @Test
    public void encrypt_tamperingAndWrongKeyDetected() {
        EncryptionService encryption = new EncryptionService(EncryptionService.generateKey());
        String stored = encryption.encrypt("alice@example.com");
        byte[] raw = Base64.getDecoder().decode(stored);
        raw[raw.length - 1] ^= 0x01;
        String tampered = Base64.getEncoder().encodeToString(raw);

        assertThrows(CryptoException.class, () -> encryption.decrypt(tampered), "a flipped bit must be detected");
        EncryptionService otherKey = new EncryptionService(EncryptionService.generateKey());
        assertThrows(CryptoException.class, () -> otherKey.decrypt(stored), "the wrong key must be detected");
        assertThrows(ValidationException.class, () -> encryption.encrypt(null), "null plaintext is rejected");
    }

    @Test
    public void register_storesEmailEncrypted() {
        InMemoryAnalystRepository repository = new InMemoryAnalystRepository();
        AuthService auth = authService(repository, new EncryptionService(EncryptionService.generateKey()));

        Analyst alice = auth.register("alice", PASSWORD.toCharArray(), "  alice@example.com ");

        String stored = repository.findByUsername("alice").orElseThrow().getEncryptedContactEmail();
        assertFalse(stored.contains("alice@example.com"), "the email must never be stored in plaintext");
        assertFalse(stored.contains("example"), "not even partially");
        assertEquals("alice@example.com", auth.decryptContactEmail(alice),
                "the email is ENCRYPTED (two-way), not hashed -- it must decrypt back (stripped)");
    }

    private static int countAnalysts(InMemoryAnalystRepository repository) {
        int count = 0;
        for (String name : List.of("alice", "bob", "1bob")) {
            if (repository.findByUsername(name).isPresent()) {
                count++;
            }
        }
        return count;
    }
}
