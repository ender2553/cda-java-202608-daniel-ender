package com.cyberdev.pos.day3;

/**
 * THE ONLY SEALED TYPE IN THE ENTIRE SERIES. A closed set of outcomes from asking a
 * payment processor to authorize a charge. Because the permits list is closed, a switch
 * over this type can be checked by the compiler for exhaustiveness -- no `default` branch
 * is needed (or wanted).
 *
 * INSTRUCTOR NOTE: deliberately kept OUTSIDE the com.cyberdev.pos.exception / PosException
 * hierarchy -- see the INSTRUCTOR NOTE on PosException for the full contrast. Short version:
 * a decline is an expected, anticipated business outcome (data returned from authorize()),
 * not a defect to throw about. Callers must switch on this exhaustively rather than
 * catch/handle it as an exception.
 */
public sealed interface AuthorizationResult permits Approved, Declined, Error {
}
