package com.cyberdev.pos.day3;

import com.cyberdev.pos.day1.CardBrand;
import com.cyberdev.pos.day1.CreditCard;
import com.cyberdev.pos.day1.GiftCard;
import com.cyberdev.pos.day1.PaymentMethod;
import com.cyberdev.pos.exception.ValidationException;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Simulated card processor. `method` and `amount` cross a trust boundary here: they may
 * have been built from user/cashier input elsewhere in the app, so this class treats every
 * field as untrusted and validates before doing anything with it.
 */
public class SimulatedProcessorAuthorizer implements PaymentAuthorizer {

    // NOTE [POS3-3]: This is the trust-boundary validation TODO. Concept
    // tested: fail-closed input handling at the boundary where "plausible but bad" data
    // enters -- an expired card, an unrecognized brand, a zero/negative amount are all
    // things a real caller (a broken UI, a replay, a tampered request) could plausibly
    // send, and each one must resolve to Declined/Error, NOT an exception that could
    // crash the checkout flow or (worse) be caught somewhere upstream and accidentally
    // treated as success. Only a null `method`/`amount` -- which indicates a structural
    // programming bug in the CALLER, not adversarial input -- is allowed to throw, and it
    // throws this project's own ValidationException (com.cyberdev.pos.exception) rather
    // than a bare NullPointerException/IllegalArgumentException, exactly the same reuse
    // of Day 1/Day 2's exception type discussed on Money.
    // Common near-miss: throwing ValidationException/IllegalArgumentException for expired
    // cards or an UNKNOWN brand instead of returning Declined. That "passes" a naive test
    // that only checks something gets caught, but fails the actual lesson (fail-closed
    // means the AUTHORIZATION RESULT reflects the failure, not an uncaught exception
    // propagating out of a payment path in production). The graded test asserts
    // `instanceof Declined`/`instanceof Error`, not just "something got thrown."
    // TODO [POS3-3]: Implement the trust-boundary validation described above:
    //   - null method/amount -> throw ValidationException (programmer error)
    //   - amount.amount().signum() <= 0 -> return Declined
    //   - CreditCard: expired (use CreditCard.isExpired against today's year/month) ->
    //     Declined; CardBrand.UNKNOWN -> Declined; otherwise approve()
    //   - GiftCard -> approve()
    //   - any other PaymentMethod subtype -> return Error (an operational/integration
    //     problem, not a business decline)
    // Fail closed: bad-but-plausible input must resolve to Declined/Error, never throw.
    @Override
    public AuthorizationResult authorize(PaymentMethod method, Money amount) {
        if (method == null) {
            throw new ValidationException("Payment method must not be null");
        }

        if (amount == null) {
            throw new ValidationException("Money amount must not be null");
        }

        if (amount.amount().signum() <= 0) {
            return new Declined("Payment amount must be greater than zero");
        }

        if (method instanceof CreditCard creditCard) {
            LocalDate today = LocalDate.now();

            if (creditCard.isExpired(today.getYear(), today.getMonthValue())) {
                return new Declined("Credit card is expired");
            }

            if (creditCard.getBrand() == CardBrand.UNKNOWN) {
                return new Declined("Credit card brand is not recognized");
            }

            return approve(amount);
        }

        if (method instanceof GiftCard) {
            return approve(amount);
        }

        return new Error("Unsupported payment method");
    }

    private AuthorizationResult approve(Money amount) {
        String authCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new Approved(authCode, amount);
    }
}
