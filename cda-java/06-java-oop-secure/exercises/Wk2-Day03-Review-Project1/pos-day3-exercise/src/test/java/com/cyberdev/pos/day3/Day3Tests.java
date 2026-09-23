package com.cyberdev.pos.day3;

import com.cyberdev.pos.day1.CardBrand;
import com.cyberdev.pos.day1.Cart;
import com.cyberdev.pos.day1.CreditCard;
import com.cyberdev.pos.day1.GiftCard;
import com.cyberdev.pos.day1.LineItem;
import com.cyberdev.pos.day1.PaymentMethod;
import com.cyberdev.pos.day1.Product;
import com.cyberdev.pos.day2.TransactionService;
import com.cyberdev.pos.day2.inmemory.InMemoryTransactionRepository;
import com.cyberdev.pos.day3.inmemory.InMemoryCashierAccountRepository;
import com.cyberdev.pos.exception.AuthenticationException;
import com.cyberdev.pos.exception.CryptoException;
import com.cyberdev.pos.exception.PosException;
import com.cyberdev.pos.exception.ValidationException;
import com.cyberdev.pos.testkit.GradedTest;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * All Day 3 graded behavior runs against InMemory* repositories -- none of it needs a
 * database, since the graded surface is CashierAuthService/CheckoutService/CardVault logic,
 * not the JDBC persistence mechanics Day 2 already tested thoroughly. The
 * CashierAccountRepository JDBC/RowMapper trio is GIVEN infrastructure (not graded here),
 * mirroring Day 2's own Merchant persistence, so it is exercised only indirectly (it is not
 * re-tested against a live database the way Day2Tests DB-gates JdbcTransactionRepository).
 */
public class Day3Tests {

    // ---------------------------------------------------------------
    // POS3-1: Money compact constructor validation
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-1", points = 3, description = "Money rejects null/negative amount and unrecognized currency with ValidationException")
    public void money_rejectsBadInput() {
        assertThrows(ValidationException.class, () -> new Money(null, "USD"), "null amount must throw ValidationException");
        assertThrows(ValidationException.class, () -> new Money(new BigDecimal("-1.00"), "USD"), "negative amount must throw ValidationException");
        assertThrows(ValidationException.class, () -> new Money(BigDecimal.TEN, "XXX"), "unrecognized currency must throw ValidationException");
        assertThrows(ValidationException.class, () -> new Money(BigDecimal.TEN, null), "null currency must throw ValidationException");
    }
    @GradedTest(tag = "POS3-1", points = 3, description = "Money normalizes currency code case and actually stores the normalized value")
    public void money_normalizesCurrencyCode() {
        Money m = new Money(BigDecimal.TEN, "usd");
        assertEquals("USD", m.currencyCode(), "currency code must be normalized to uppercase AND actually stored (not just validated on a local variable)");
        assertDoesNotThrow(() -> new Money(new BigDecimal("10.00"), "USD"), "valid Money must construct without throwing");
    }

    // ---------------------------------------------------------------
    // POS3-2: sealed AuthorizationResult + record leaves, kept OUT of PosException
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-2", points = 3, description = "AuthorizationResult is sealed with exactly Approved/Declined/Error permitted")
    public void authorizationResult_isSealedWithExactPermits() {
        assertTrue(AuthorizationResult.class.isSealed(), "AuthorizationResult must be a sealed interface");
        Class<?>[] permitted = AuthorizationResult.class.getPermittedSubclasses();
        assertNotNull(permitted, "sealed interface must declare a permits list");
        String permittedNames = Arrays.toString(permitted);
        assertTrue(permittedNames.contains("Approved") && permittedNames.contains("Declined") && permittedNames.contains("Error"),
                "permits list must be exactly Approved, Declined, Error: was " + permittedNames);
        assertEquals(3, permitted.length, "permits list must contain exactly three types");
    }
    @GradedTest(tag = "POS3-2", points = 2, description = "Approved/Declined/Error are records that validate their own invariants")
    public void authorizationResult_leavesValidateInvariants() {
        Money amt = new Money(BigDecimal.TEN, "USD");
        assertDoesNotThrow(() -> new Approved("AUTH-1", amt));
        assertDoesNotThrow(() -> new Declined("insufficient funds"));
        assertDoesNotThrow(() -> new Error("processor timeout"));

        assertThrows(ValidationException.class, () -> new Approved("", amt), "blank authCode must throw");
        assertThrows(ValidationException.class, () -> new Approved(null, amt), "null authCode must throw");
        assertThrows(ValidationException.class, () -> new Declined(""), "blank reason must throw");
        assertThrows(ValidationException.class, () -> new Error(null), "null message must throw");
    }
    @GradedTest(tag = "POS3-2", points = 2, description = "AuthorizationResult is deliberately NOT part of the PosException hierarchy")
    public void authorizationResult_isNotAPosException() {
        Money amt = new Money(BigDecimal.TEN, "USD");
        assertFalse(PosException.class.isAssignableFrom(Approved.class), "Approved must not extend PosException -- it is data returned from authorize(), never thrown");
        assertFalse(PosException.class.isAssignableFrom(Declined.class), "Declined must not extend PosException -- a decline is an expected business outcome, not a defect");
        assertFalse(PosException.class.isAssignableFrom(Error.class), "Error (the AuthorizationResult case) must not extend PosException");
        assertFalse(Throwable.class.isAssignableFrom(AuthorizationResult.class), "AuthorizationResult must not be Throwable at all");
    }

    // ---------------------------------------------------------------
    // POS3-3: SimulatedProcessorAuthorizer trust-boundary validation
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-3", points = 3, description = "Authorizer fails closed to Declined for expired card and UNKNOWN brand, never throws")
    public void authorizer_failsClosedOnPlausibleBadInput() {
        SimulatedProcessorAuthorizer authorizer = new SimulatedProcessorAuthorizer();
        Money amount = new Money(new BigDecimal("20.00"), "USD");

        CreditCard expired = new CreditCard("Jane Doe", "1111", CardBrand.VISA, 1, 2000);
        AuthorizationResult expiredResult = assertDoesNotThrow(() -> authorizer.authorize(expired, amount),
                "an expired card must never throw out of authorize()");
        assertInstanceOf(Declined.class, expiredResult, "expired card must be Declined, not thrown or approved");

        CreditCard unknownBrand = new CreditCard("Jane Doe", "1111", CardBrand.UNKNOWN, 12, 2099);
        AuthorizationResult unknownResult = assertDoesNotThrow(() -> authorizer.authorize(unknownBrand, amount));
        assertInstanceOf(Declined.class, unknownResult, "UNKNOWN brand must be Declined, not thrown");
    }
    @GradedTest(tag = "POS3-3", points = 3, description = "Authorizer fails closed on a non-positive amount and approves genuinely valid input")
    public void authorizer_nonPositiveAmountDeclinedValidInputApproved() {
        SimulatedProcessorAuthorizer authorizer = new SimulatedProcessorAuthorizer();
        CreditCard validCard = new CreditCard("Jane Doe", "1111", CardBrand.VISA, 12, 2099);

        Money zero = new Money(BigDecimal.ZERO, "USD");
        assertInstanceOf(Declined.class, authorizer.authorize(validCard, zero), "zero amount must be Declined");

        Money amount = new Money(new BigDecimal("20.00"), "USD");
        AuthorizationResult approvedResult = authorizer.authorize(validCard, amount);
        assertInstanceOf(Approved.class, approvedResult, "a valid, non-expired, recognized-brand card with a positive amount should be Approved");
    }
    @GradedTest(tag = "POS3-3", points = 2, description = "Authorizer throws ValidationException (not a bare NPE/IAE) for structurally null constructor-boundary input")
    public void authorizer_nullMethodOrAmountThrowsValidationException() {
        SimulatedProcessorAuthorizer authorizer = new SimulatedProcessorAuthorizer();
        Money amount = new Money(BigDecimal.TEN, "USD");
        CreditCard card = new CreditCard("Jane Doe", "1111", CardBrand.VISA, 12, 2099);

        assertThrows(ValidationException.class, () -> authorizer.authorize(null, amount),
                "a null PaymentMethod is a programmer error and should throw this project's ValidationException");
        assertThrows(ValidationException.class, () -> authorizer.authorize(card, null),
                "a null Money amount is a programmer error and should throw this project's ValidationException");
    }

    // ---------------------------------------------------------------
    // POS3-4: CheckoutService's switch is exhaustive over AuthorizationResult, no default
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-4", points = 8, description = "CheckoutService.checkout's switch has no default branch and handles all three AuthorizationResult cases distinctly")
    public void checkoutSwitch_hasNoDefaultAndHandlesAllThreeCasesDistinctly() throws IOException {
        String source = readCheckoutServiceSource();
        assertFalse(source.contains("default ->") || source.contains("default:"),
                "checkout()'s switch over the sealed AuthorizationResult must not contain a default branch -- "
                        + "a default branch defeats the whole point of sealing the hierarchy (a future 4th subtype "
                        + "would compile silently instead of forcing a new case here)");
        assertTrue(source.contains("case Approved"), "switch must have an explicit case for Approved");
        assertTrue(source.contains("case Declined"), "switch must have an explicit case for Declined");
        assertTrue(source.contains("case Error"), "switch must have an explicit case for Error");

        // Behaviorally: each of the three outcome types is handled without CheckoutService
        // throwing, and Declined/Error/Approved are handled DISTINCTLY (verified fully by
        // POS3-5's persistence check below; here we only check no outcome type crashes it).
        InMemoryTransactionRepository repo = new InMemoryTransactionRepository();
        TransactionService txnService = new TransactionService(repo);
        Cart cart = new Cart();
        cart.addItem(new LineItem(new Product("SKU-1", "Widget", BigDecimal.ONE), 1));
        PaymentMethod method = new GiftCard("Jane Doe", "1111", new BigDecimal("50.00"));

        for (PaymentAuthorizer fakeAuthorizer : new PaymentAuthorizer[]{
                (m, a) -> new Approved("AUTH-OK", a),
                (m, a) -> new Declined("no funds"),
                (m, a) -> new Error("processor timeout")
        }) {
            CheckoutService checkout = new CheckoutService(fakeAuthorizer, txnService);
            assertDoesNotThrow(() -> checkout.checkout(cart, method, "M-100"),
                    "every AuthorizationResult case must be handled without CheckoutService throwing");
        }
    }

    // ---------------------------------------------------------------
    // POS3-5: only persist via the injected TransactionService, only on Approved
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-5", points = 5, description = "CheckoutService persists via the injected TransactionService only on Approved; Declined/Error never save")
    public void checkout_persistsOnlyOnApproved() {
        InMemoryTransactionRepository repo = new InMemoryTransactionRepository();
        TransactionService txnService = new TransactionService(repo);
        Cart cart = new Cart();
        cart.addItem(new LineItem(new Product("SKU-1", "Widget", new BigDecimal("5.00")), 1));
        PaymentMethod method = new GiftCard("Jane Doe", "1111", new BigDecimal("50.00"));

        CheckoutService declinedFlow = new CheckoutService((m, a) -> new Declined("no funds"), txnService);
        declinedFlow.checkout(cart, method, "M-100");
        assertTrue(repo.searchByMemo("").isEmpty(), "Declined must not persist any transaction");

        CheckoutService errorFlow = new CheckoutService((m, a) -> new Error("timeout"), txnService);
        errorFlow.checkout(cart, method, "M-100");
        assertTrue(repo.searchByMemo("").isEmpty(), "Error must not persist any transaction");

        CheckoutService approvedFlow = new CheckoutService((m, a) -> new Approved("AUTH-OK", a), txnService);
        AuthorizationResult result = approvedFlow.checkout(cart, method, "M-100");
        assertInstanceOf(Approved.class, result);
        assertEquals(1, repo.searchByMemo("").size(), "Approved must persist exactly one transaction via the injected TransactionService");
    }
    @GradedTest(tag = "POS3-5", points = 4, description = "CheckoutService only accepts its collaborators via the constructor (DI discipline), never builds its own TransactionService/authorizer")
    public void checkoutService_constructorInjectionDiscipline() {
        Constructor<?>[] ctors = CheckoutService.class.getDeclaredConstructors();
        boolean hasExpectedConstructor = false;
        for (Constructor<?> ctor : ctors) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 2
                    && PaymentAuthorizer.class.isAssignableFrom(params[0])
                    && TransactionService.class.isAssignableFrom(params[1])) {
                hasExpectedConstructor = true;
            }
        }
        assertTrue(hasExpectedConstructor, "CheckoutService must have a constructor accepting (PaymentAuthorizer, TransactionService)");

        // Prove the SAME injected TransactionService instance is the one actually used --
        // not a second, internally-constructed one -- exactly like Day 2's POS2-5 check.
        InMemoryTransactionRepository repo = new InMemoryTransactionRepository();
        TransactionService txnService = new TransactionService(repo);
        CheckoutService checkout = new CheckoutService((m, a) -> new Approved("AUTH-DI", a), txnService);
        Cart cart = new Cart();
        cart.addItem(new LineItem(new Product("SKU-1", "Widget", BigDecimal.TEN), 1));
        checkout.checkout(cart, new GiftCard("Jane Doe", "1111", new BigDecimal("50.00")), "M-100");
        assertEquals(1, repo.searchByMemo("").size(),
                "the sale must be visible through the SAME injected TransactionRepository, proving CheckoutService used the injected TransactionService, not an internal one");
    }

    // ---------------------------------------------------------------
    // POS3-6: CashierAuthService.enroll -- PBKDF2 with fresh random salt per account
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-6", points = 7, description = "enroll() hashes with PBKDF2 using a fresh random salt per account; identical PINs hash differently")
    public void enroll_usesFreshRandomSaltPerAccount() {
        InMemoryCashierAccountRepository repository = new InMemoryCashierAccountRepository();
        CashierAuthService authService = new CashierAuthService(repository);

        authService.enroll("alice", "1234".toCharArray());
        authService.enroll("bob", "1234".toCharArray());

        CashierAccount aliceAccount = repository.findByCashierId("alice").orElseThrow();
        CashierAccount bobAccount = repository.findByCashierId("bob").orElseThrow();

        assertFalse(Arrays.equals(aliceAccount.getSalt(), bobAccount.getSalt()),
                "salts for two different accounts must differ (fresh SecureRandom salt per account, not shared/fixed)");
        assertFalse(Arrays.equals(aliceAccount.getPinHash(), bobAccount.getPinHash()),
                "two accounts enrolled with the IDENTICAL PIN must still get DIFFERENT stored hashes, because their salts differ");
        assertTrue(aliceAccount.getSalt().length >= 8, "salt should be a meaningful length, not trivially short");

        // Re-enrolling the same account (a PIN reset) must also generate a fresh salt.
        authService.enroll("alice", "1234".toCharArray());
        CashierAccount aliceReEnrolled = repository.findByCashierId("alice").orElseThrow();
        assertFalse(Arrays.equals(aliceAccount.getSalt(), aliceReEnrolled.getSalt()),
                "re-enrolling must generate a fresh salt, not reuse the previous one");
    }

    // ---------------------------------------------------------------
    // POS3-7: CashierAuthService.login -- fail-closed, indistinguishable failure modes
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-7", points = 4, description = "login() succeeds for a correct PIN and throws AuthenticationException for a wrong PIN")
    public void login_succeedsOnCorrectPinFailsOnWrongPin() {
        InMemoryCashierAccountRepository repository = new InMemoryCashierAccountRepository();
        CashierAuthService authService = new CashierAuthService(repository);
        authService.enroll("alice", "1234".toCharArray());

        assertDoesNotThrow(() -> authService.login("alice", "1234".toCharArray()), "correct PIN must log in successfully");
        assertThrows(AuthenticationException.class, () -> authService.login("alice", "0000".toCharArray()),
                "wrong PIN must throw AuthenticationException");
    }
    @GradedTest(tag = "POS3-7", points = 4, description = "login() fails closed and indistinguishably for an unknown cashier vs a wrong PIN (no enumeration oracle)")
    public void login_unknownCashierAndWrongPinAreIndistinguishable() {
        InMemoryCashierAccountRepository repository = new InMemoryCashierAccountRepository();
        CashierAuthService authService = new CashierAuthService(repository);
        authService.enroll("alice", "1234".toCharArray());

        AuthenticationException unknownUserFailure = assertThrows(AuthenticationException.class,
                () -> authService.login("nobody", "1234".toCharArray()), "unknown cashier must throw AuthenticationException");
        AuthenticationException wrongPinFailure = assertThrows(AuthenticationException.class,
                () -> authService.login("alice", "0000".toCharArray()), "wrong PIN must throw AuthenticationException");

        assertEquals(unknownUserFailure.getMessage(), wrongPinFailure.getMessage(),
                "the two distinct failure modes (unknown cashier vs wrong PIN) must produce the SAME message -- "
                        + "a distinguishable error is a username-enumeration oracle");
        assertDoesNotThrow(() -> {
            try {
                authService.login(null, "1234".toCharArray());
            } catch (AuthenticationException ignored) {
                // expected -- must fail closed, never let a different exception type escape
            }
        }, "a null cashierId must resolve to AuthenticationException, never an uncaught NullPointerException");
    }

    // ---------------------------------------------------------------
    // POS3-8: CardVault.encrypt -- fresh IV every call, AES-256-GCM
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-8", points = 8, description = "encrypt() uses a fresh random IV on every call; identical plaintext+key never produces identical output")
    public void encrypt_usesFreshIvEveryCall() {
        CardVault vault = new CardVault();
        SecretKey key = CardVault.generateKey();
        assertEquals(256, key.getEncoded().length * 8, "generateKey() must produce a 256-bit AES key");

        byte[] first = vault.encrypt("customer@example.com", key);
        byte[] second = vault.encrypt("customer@example.com", key);

        assertFalse(Arrays.equals(first, second),
                "two encryptions of the SAME plaintext with the SAME key must never produce identical output -- "
                        + "that can only happen if the IV was reused");
        // The first 12 bytes of each output are the IV; they must differ between calls.
        byte[] firstIv = Arrays.copyOfRange(first, 0, 12);
        byte[] secondIv = Arrays.copyOfRange(second, 0, 12);
        assertFalse(Arrays.equals(firstIv, secondIv), "the embedded IV must differ between two separate encrypt() calls");
    }

    // ---------------------------------------------------------------
    // POS3-9: CardVault.decrypt -- round-trips, tampering raises CryptoException
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-9", points = 3, description = "decrypt() round-trips correctly for untampered data")
    public void decrypt_roundTripsCorrectly() {
        CardVault vault = new CardVault();
        SecretKey key = CardVault.generateKey();
        String original = "customer@example.com";

        byte[] encrypted = vault.encrypt(original, key);
        String decrypted = vault.decrypt(encrypted, key);
        assertEquals(original, decrypted, "decrypting with the correct key must recover the original plaintext exactly");
    }
    @GradedTest(tag = "POS3-9", points = 3, description = "decrypt() raises this project's CryptoException (not a raw crypto exception) on tampered ciphertext")
    public void decrypt_tamperedCiphertextRaisesCryptoException() {
        CardVault vault = new CardVault();
        SecretKey key = CardVault.generateKey();
        byte[] encrypted = vault.encrypt("customer@example.com", key);

        byte[] tampered = encrypted.clone();
        tampered[tampered.length - 1] ^= (byte) 0xFF; // flip a bit inside the ciphertext/tag

        assertThrows(CryptoException.class, () -> vault.decrypt(tampered, key),
                "tampered ciphertext must fail the GCM authentication check and surface as this project's CryptoException, "
                        + "never a raw javax.crypto/java.security exception and never silently returning garbage");
        assertTrue(PosException.class.isAssignableFrom(CryptoException.class), "CryptoException must extend PosException");
    }

    // ---------------------------------------------------------------
    // POS3-10: login gate blocks checkout when login fails
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-10", points = 7, description = "A failed cashier login blocks checkout entirely -- no sale is ever recorded")
    public void loginGate_blocksCheckoutOnFailure() {
        InMemoryCashierAccountRepository cashierRepo = new InMemoryCashierAccountRepository();
        CashierAuthService authService = new CashierAuthService(cashierRepo);
        authService.enroll("alice", "1234".toCharArray());

        InMemoryTransactionRepository txnRepo = new InMemoryTransactionRepository();
        TransactionService txnService = new TransactionService(txnRepo);
        CheckoutService checkoutService = new CheckoutService((m, a) -> new Approved("AUTH-OK", a), txnService);

        Cart cart = new Cart();
        cart.addItem(new LineItem(new Product("SKU-1", "Coffee", new BigDecimal("3.50")), 1));
        PaymentMethod method = new GiftCard("Jane Doe", "1111", new BigDecimal("50.00"));

        // This mirrors exactly what Main does: attempt login, and only proceed to checkout
        // if it succeeds. A wrong PIN must mean checkout() is NEVER called at all.
        boolean checkoutRan = false;
        try {
            authService.login("alice", "wrong-pin".toCharArray());
            checkoutService.checkout(cart, method, "M-100");
            checkoutRan = true;
        } catch (AuthenticationException expected) {
            // login gate correctly blocked progress
        }

        assertFalse(checkoutRan, "checkout must never run after a failed login");
        assertTrue(txnRepo.searchByMemo("").isEmpty(), "no transaction may be recorded when the login gate blocks checkout");
    }

    // ---------------------------------------------------------------
    // POS3-11: full assembled flow, InMemory repositories end to end
    // ---------------------------------------------------------------
    @GradedTest(tag = "POS3-11", points = 6, description = "Full flow: login -> cart -> authorize -> exhaustive switch -> persist only on Approved, end to end")
    public void fullFlow_loginThenCheckoutPersistsExactlyOneApprovedSale() {
        InMemoryCashierAccountRepository cashierRepo = new InMemoryCashierAccountRepository();
        CashierAuthService authService = new CashierAuthService(cashierRepo);
        authService.enroll("alice", "1234".toCharArray());

        InMemoryTransactionRepository txnRepo = new InMemoryTransactionRepository();
        TransactionService txnService = new TransactionService(txnRepo);
        PaymentAuthorizer authorizer = new SimulatedProcessorAuthorizer();
        CheckoutService checkoutService = new CheckoutService(authorizer, txnService);

        assertDoesNotThrow(() -> authService.login("alice", "1234".toCharArray()), "correct login must succeed");

        Cart cart = new Cart();
        cart.addItem(new LineItem(new Product("SKU-1", "Coffee", new BigDecimal("3.50")), 2));
        cart.addItem(new LineItem(new Product("SKU-2", "Bagel", new BigDecimal("2.25")), 1));
        PaymentMethod method = new CreditCard("Alice Cashier", "4242", CardBrand.VISA, 12, 2099);

        AuthorizationResult result = checkoutService.checkout(cart, method, "M-100");
        assertInstanceOf(Approved.class, result, "a valid card and positive amount should be approved by the simulated processor");
        assertEquals(1, txnRepo.searchByMemo("").size(), "exactly one sale must be persisted end to end");

        // A second, DECLINED-by-construction authorizer must persist nothing more.
        CheckoutService declinedCheckout = new CheckoutService((m, a) -> new Declined("no funds"), txnService);
        declinedCheckout.checkout(cart, method, "M-100");
        assertEquals(1, txnRepo.searchByMemo("").size(), "a subsequent Declined checkout must not add a second persisted sale");
    }

    // ---------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------

    private static String readCheckoutServiceSource() throws IOException {
        Path path = Path.of("src", "main", "java", "com", "cyberdev", "pos", "day3", "CheckoutService.java");
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
