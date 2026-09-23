package com.cyberdev.pos.day3;

import com.cyberdev.pos.exception.AuthenticationException;
import com.cyberdev.pos.exception.ValidationException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Enrolls and logs in cashiers using PBKDF2WithHmacSHA256 with a fresh random salt per
 * account. Never stores or logs the raw PIN.
 *
 * NOTE on DI discipline: exactly like Day 2's TransactionService (POS2-5), the
 * ONLY way this class gets a CashierAccountRepository is through its constructor -- there
 * is no no-arg constructor that builds an InMemoryCashierAccountRepository internally. A
 * test can hand this an InMemoryCashierAccountRepository (fast, no DB) or a
 * JdbcCashierAccountRepository (real Postgres) without this class's own code changing at
 * all.
 */
public final class CashierAuthService {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private final CashierAccountRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public CashierAuthService(CashierAccountRepository repository) {
        if (repository == null) {
            throw new ValidationException("repository must not be null");
        }
        this.repository = repository;
    }

    // INSTRUCTOR NOTE [POS3-6]: Concept tested: PBKDF2 PIN hashing with a FRESH random
    // salt generated PER ACCOUNT (never a fixed/shared salt, never derived from the
    // cashierId). Why it matters: a unique salt per account defeats precomputed
    // rainbow-table attacks and ensures two cashiers with the same PIN ("1234") get
    // completely different stored hashes. Common mistakes: (1) reusing one static salt
    // for every account -- compiles, "hashes" the PIN, but two identical PINs produce
    // identical hashes, which leaks information and re-enables rainbow tables; (2) using
    // MessageDigest.getInstance("SHA-256") directly on the PIN instead of PBKDF2 -- a fast
    // hash with no work factor, crackable by brute force in a database breach; (3) using
    // Math.random() or `new Random()` (not SecureRandom) for the salt, which is
    // predictable.
    // TODO [POS3-6]: Validate cashierId/pin (ValidationException on failure), generate a
    // FRESH byte[SALT_LENGTH_BYTES] salt via `secureRandom` for THIS call, hash the pin with
    // pbkdf2(pin, salt), and save a new CashierAccount via the injected repository. Every
    // call (including a re-enrollment of the same cashierId) must generate its own fresh
    // salt -- never a shared/static one.
    public void enroll(String cashierId, char[] pin) {
        if (cashierId == null || cashierId.isBlank()) {
            throw new ValidationException("cashierId must not be blank");
        }

        if (pin == null || pin.length == 0) {
            throw new ValidationException("pin must not be null or empty");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);

        byte[] pinHash = pbkdf2(pin, salt);

        repository.save(new CashierAccount(cashierId, salt, pinHash));
    }

    // INSTRUCTOR NOTE [POS3-7]: Concept tested: fail-closed login that does NOT let a
    // caller distinguish "unknown cashierId" from "known cashierId, wrong PIN" -- both
    // resolve to the exact same AuthenticationException with the exact same generic
    // message. This matters because a distinguishable error (e.g. "no such cashier" vs
    // "wrong PIN") is a username-enumeration oracle: an attacker could script logins
    // against every cashierId guess and learn which ones exist from the error alone, long
    // before ever guessing a correct PIN. Also tested implicitly: recomputing the hash
    // with the ACCOUNT'S STORED salt (not a new random one) -- a fresh salt per login
    // attempt would never match. ANY failure mode here -- null/blank input, an unexpected
    // checked exception from the crypto provider -- must resolve to a thrown
    // AuthenticationException, never let some other exception type escape (a login screen
    // crashing with a raw NullPointerException is a usability AND security smell: stack
    // traces can leak information, and it trains callers to wrap this in a broad
    // try/catch that might accidentally treat an exception as "logged in").
    // Common near-miss: using `==` or `Arrays.equals` for byte[] comparison instead of
    // MessageDigest.isEqual -- functionally fine for value-equality in this teaching
    // exercise (not a timing-attack-hardened production system), but MessageDigest.isEqual
    // is the security-conscious idiom students should learn to reach for by default for
    // secret comparison; flag Arrays.equals as a near-miss in manual review even though the
    // automated test can't always tell the difference.
    // TODO [POS3-7]: Look up the account by cashierId, recompute the PBKDF2 hash of `pin`
    // using the ACCOUNT'S STORED salt, and compare against the stored hash with
    // MessageDigest.isEqual (not Arrays.equals/==). On ANY failure -- null/blank input,
    // unknown cashierId, wrong PIN, or any unexpected exception -- throw
    // AuthenticationException with the SAME generic message every time; a caller must never
    // be able to tell "unknown cashier" apart from "known cashier, wrong PIN" from the
    // exception alone (that distinguishability is a username-enumeration oracle). Return
    // normally (no exception) only when the PIN is correct.
    public void login(String cashierId, char[] pin) {
        final String failureMessage = "Authentication failed";

        try {
            if (cashierId == null || cashierId.isBlank()) {
                throw new AuthenticationException(failureMessage);
            }

            if (pin == null || pin.length == 0) {
                throw new AuthenticationException(failureMessage);
            }

            CashierAccount account = repository.findByCashierId(cashierId)
                    .orElseThrow(() -> new AuthenticationException(failureMessage));

            byte[] computedHash = pbkdf2(pin, account.getSalt());

            if (!MessageDigest.isEqual(computedHash, account.getPinHash())) {
                throw new AuthenticationException(failureMessage);
            }
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException(failureMessage);
        }
    }

    private byte[] pbkdf2(char[] pin, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(pin, salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 algorithm unavailable", e);
        }
    }
}
