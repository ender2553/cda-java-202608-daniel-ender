package com.cyberdev.pos.day3;

import com.cyberdev.pos.day1.Cart;
import com.cyberdev.pos.day1.PaymentMethod;
import com.cyberdev.pos.day2.TransactionService;
import com.cyberdev.pos.exception.ValidationException;

/**
 * The key integration point for Day 3: ties this day's PaymentAuthorizer to Day 2's real
 * TransactionService (not a repository directly -- CheckoutService goes through the same
 * service layer Day 2 already built, matching Day 2's own layering discipline).
 */
public final class CheckoutService {

    private final PaymentAuthorizer authorizer;
    private final TransactionService transactionService;

    // NOTE [POS3-5, DI half]: Both collaborators arrive ONLY through this
    // constructor -- exactly the same manual-IoC discipline as Day 2's TransactionService
    // (POS2-5). CheckoutService must never call `new SimulatedProcessorAuthorizer(...)` or
    // `new TransactionService(...)` internally "for convenience"; doing so would defeat
    // testability (a test could no longer substitute a fake authorizer/repository) even if
    // it doesn't show up as a compile error. Reusing TransactionService here -- rather than
    // reaching past it to a raw TransactionRepository -- is deliberate: Day 2 already
    // taught duplicate-id rejection and DuplicateTransactionException inside
    // TransactionService.recordSale, and CheckoutService should not reinvent that logic.
    public CheckoutService(PaymentAuthorizer authorizer, TransactionService transactionService) {
        if (authorizer == null) {
            throw new ValidationException("authorizer must not be null");
        }
        if (transactionService == null) {
            throw new ValidationException("transactionService must not be null");
        }
        this.authorizer = authorizer;
        this.transactionService = transactionService;
    }

    /**
     * Authorizes the cart's total against {@code method} and, only on approval, records
     * the sale via the injected TransactionService. Returns the AuthorizationResult so the
     * caller can inspect it (e.g. to print a receipt or a decline reason).
     */
    // TODO [POS3-4 / POS3-5]: Build `Money total = new Money(cart.getTotal(), "USD")`, call
    // `authorizer.authorize(method, total)`, then switch EXHAUSTIVELY over the sealed
    // AuthorizationResult (case Approved / Declined / Error, NO default branch -- letting
    // the compiler enforce exhaustiveness is the whole point of the sealed hierarchy).
    // Only the Approved case may call `transactionService.recordSale(cart, merchantId)`;
    // Declined and Error must persist nothing. Return the AuthorizationResult either way.
    public AuthorizationResult checkout(Cart cart, PaymentMethod method, String merchantId) {
        throw new UnsupportedOperationException(
                "TODO [POS3-4/POS3-5]: authorize, then an exhaustive no-default switch that persists via transactionService ONLY on Approved");
    }
}
