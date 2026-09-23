package com.cyberdev.pos.exception;

/**
 * Day 3 (additive -- Day 1/Day 2's exception files are unmodified).
 *
 * Thrown by CashierAuthService.login when a login attempt fails, for ANY reason: an
 * unknown cashier id, or a known cashier id with the wrong PIN. Both cases throw this
 * exact type with the same generic message deliberately -- see the INSTRUCTOR NOTE on
 * CashierAuthService.login (POS3-7) for why the two failure modes must not be
 * distinguishable to the caller (a distinguishable error lets an attacker enumerate valid
 * cashier ids one guess at a time, an "unknown-user oracle").
 *
 * Naming matches the same convention already used in this curriculum's sibling dndrpg
 * project (an *Exception type per distinct failure mode, extending a shared base).
 */
public class AuthenticationException extends PosException {

    public AuthenticationException(String message) {
        super(message);
    }
}
