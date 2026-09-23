package com.cyberdev.dndrpg.exception;

/**
 * DAY 4 ADDITION (additive -- Day 1/2/3's exception files are unmodified).
 *
 * Thrown by AuthService.login when a login attempt fails, for ANY reason:
 * an unknown username, or a known username with the wrong password. Both
 * cases throw this exact type with the same generic message deliberately
 * -- see AuthService.login's INSTRUCTOR NOTE for why the two failure modes
 * must not be distinguishable to the caller (a distinguishable error lets
 * an attacker enumerate valid usernames one guess at a time, a
 * "username-enumeration oracle"). Also thrown by AuthService.register when
 * the requested username is already taken.
 */
public class AuthenticationException extends GameException {
    public AuthenticationException(String message) {
        super(message);
    }
}
