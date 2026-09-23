package com.cyberdev.pos.day3;

import com.cyberdev.pos.day1.CardBrand;
import com.cyberdev.pos.day1.Cart;
import com.cyberdev.pos.day1.CreditCard;
import com.cyberdev.pos.day1.LineItem;
import com.cyberdev.pos.day1.Product;
import com.cyberdev.pos.day2.Merchant;
import com.cyberdev.pos.day2.TransactionService;
import com.cyberdev.pos.day2.inmemory.InMemoryMerchantRepository;
import com.cyberdev.pos.day2.inmemory.InMemoryTransactionRepository;
import com.cyberdev.pos.day3.inmemory.InMemoryCashierAccountRepository;
import com.cyberdev.pos.exception.AuthenticationException;

import javax.crypto.SecretKey;
import java.math.BigDecimal;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO. This class is provided fully working, but it
 * calls into several methods that ARE graded TODOs elsewhere in this package
 * (SimulatedProcessorAuthorizer.authorize, CheckoutService.checkout, CashierAuthService.
 * enroll/login, CardVault.encrypt/decrypt) -- it will not run end to end until you have
 * implemented those. Read it as a worked example of how all the pieces are meant to fit
 * together, not as something you need to (or should) modify.
 *
 * QuickPay POS -- Day 3 console demo assembling Day 1 (Cart/PaymentMethod), Day 2
 * (Merchant/TransactionService, real persistence layer) and Day 3 (authorization, cashier
 * login, PII encryption) into one checkout flow.
 *
 * Flow: register a merchant -> enroll + login a cashier (login gate blocks everything
 * after it on failure) -> build a cart -> authorize + checkout (exhaustive switch, persist
 * only on Approved) -> encrypt a PII-shaped value (a customer email) with CardVault, shown
 * side by side with the cashier PIN's PBKDF2 hash, to make the hash-vs-encrypt distinction
 * concrete: a PIN is HASHED (one-way, only ever compared, never recovered) while an email
 * we need to read back later is ENCRYPTED (two-way, recoverable with the key).
 *
 * All repositories here default to the InMemory* implementations so this runs with zero
 * setup (`mvn exec:java` or running the compiled class directly, no database needed).
 * Swapping to the Jdbc* implementations is a drop-in: build a JdbcTemplate from
 * DatabaseConfig.createDataSource() and pass JdbcMerchantRepository/
 * JdbcTransactionRepository/JdbcCashierAccountRepository instead, with schema.sql AND
 * day3_schema.sql applied to a running Postgres -- none of the service classes
 * (TransactionService, CashierAuthService, CheckoutService) change at all.
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        // --- Wiring: everything defaults to InMemory* repositories, no DB required ---
        InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository();
        InMemoryTransactionRepository transactionRepository = new InMemoryTransactionRepository();
        InMemoryCashierAccountRepository cashierAccountRepository = new InMemoryCashierAccountRepository();

        TransactionService transactionService = new TransactionService(transactionRepository);
        CashierAuthService authService = new CashierAuthService(cashierAccountRepository);
        PaymentAuthorizer authorizer = new SimulatedProcessorAuthorizer();
        CheckoutService checkoutService = new CheckoutService(authorizer, transactionService);

        // --- Register a merchant (Day 2) ---
        merchantRepository.register(new Merchant("M-100", "QuickPay Demo Store"));

        // --- Enroll a demo cashier, then log in (Day 3) ---
        authService.enroll("alice", "1234".toCharArray());

        // INSTRUCTOR NOTE [POS3-10]: The login gate. Concept tested: wiring
        // CashierAuthService.login into a real control-flow gate that actually blocks
        // progress on failure -- login() throws AuthenticationException, and this catch
        // block returns from main() immediately, never reaching the cart/checkout code
        // below. Common near-miss: calling login() and printing a warning but still
        // falling through to checkout -- that "compiles and demos fine" if the person
        // testing it always logs in correctly, but a negative-control test (wrong PIN)
        // would catch that checkout still ran.
        System.out.println("=== QuickPay POS (Day 3) ===");
        try {
            authService.login("alice", "1234".toCharArray());
        } catch (AuthenticationException e) {
            System.out.println("Access denied: invalid cashier credentials. Exiting.");
            return;
        }
        System.out.println("Welcome, alice.");

        // --- Build a cart (Day 1, given) ---
        Cart cart = new Cart();
        cart.addItem(new LineItem(new Product("SKU-1", "Coffee", new BigDecimal("3.50")), 2));
        cart.addItem(new LineItem(new Product("SKU-2", "Bagel", new BigDecimal("2.25")), 1));
        System.out.println("Cart total: $" + cart.getTotal());

        CreditCard paymentMethod = new CreditCard("Alice Cashier", "4242", CardBrand.VISA, 12, 2030);

        // --- Checkout (Day 3 CheckoutService: authorize, then exhaustive switch) ---
        // INSTRUCTOR NOTE [POS3-11]: The full assembled flow, one more level up from
        // CheckoutService's own internal switch -- notice both switches are exhaustive
        // with no default, reinforcing the same idiom twice for pattern-matching muscle
        // memory. On Approved, and ONLY on Approved, the sale is persisted (inside
        // CheckoutService) and a PII-shaped value gets vaulted here -- Declined/Error must
        // never reach CardVault since there's nothing legitimate to store yet.
        AuthorizationResult result = checkoutService.checkout(cart, paymentMethod, "M-100");

        switch (result) {
            case Approved approved -> {
                CardVault vault = new CardVault();
                SecretKey key = CardVault.generateKey();

                // Side-by-side demo of hash-vs-encrypt: the cashier's PIN was HASHED
                // (one-way, PBKDF2, never recoverable) back in enroll(); this customer
                // email is ENCRYPTED (two-way, AES-256-GCM) because the business
                // legitimately needs to read it back later (e.g. to email a receipt) --
                // a PIN never has that requirement, which is exactly why the two use
                // different cryptographic primitives.
                String customerEmail = "customer@example.com";
                byte[] encryptedEmail = vault.encrypt(customerEmail, key);
                String decryptedEmail = vault.decrypt(encryptedEmail, key);

                String maskedRef = CardVault.maskedReference(paymentMethod.getLast4(), approved.authCode());

                System.out.println();
                System.out.println("=== Receipt ===");
                System.out.println("Auth code: " + approved.authCode());
                System.out.println("Amount charged: $" + approved.amount().amount() + " " + approved.amount().currencyCode());
                System.out.println("Card on file: " + maskedRef);
                System.out.println("Encrypted email bytes stored: " + encryptedEmail.length
                        + " (never storing the raw address in plaintext)");
                System.out.println("Decrypted email round-trip check: " + decryptedEmail.equals(customerEmail));
                System.out.println("Thank you for shopping with QuickPay!");
            }
            case Declined declined -> {
                System.out.println();
                System.out.println("Payment declined: " + declined.reason());
            }
            case Error error -> {
                System.out.println();
                System.out.println("Payment processor error: " + error.message());
            }
        }
    }
}
