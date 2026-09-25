package com.cyberdev.secsuite.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Base RuntimeException for the whole SecOps Analyst Suite. Every custom exception in this
 * capstone extends it, so a caller that wants one broad "something in SecSuite-land went
 * wrong" catch (Main's top-level handler) can catch SecSuiteException once instead of
 * enumerating every subtype.
 *
 * INSTRUCTOR NOTE: this hierarchy is deliberately separate from the sealed
 * com.cyberdev.secsuite.event.RiskAssessment (LowRisk/MediumRisk/HighRisk/CriticalRisk) --
 * the same contrast QuickPay POS drew between its thrown PosException hierarchy and its
 * returned AuthorizationResult:
 *   - A risk being classified CRITICAL is not an error. It is an EXPECTED, normal answer to
 *     "how bad is this?", so it is RETURNED as data from RiskRegisterService.assess(...) and
 *     handled with an exhaustive switch the compiler can check.
 *   - The exceptions in this package model genuinely EXCEPTIONAL conditions: malformed input
 *     (an IP address that is not an IP address), a duplicate write that must be refused, a
 *     missing input file, a failed login, a crypto failure, a report that could not be
 *     written. There is no sensible "data" value to return in their place.
 * Discussion prompt: "why does a CRITICAL risk come back as a return value, but a malformed
 * CSV file throws?"
 *
 * Unchecked (RuntimeException) on purpose, consistent with the rest of the program: callers
 * are not forced to write empty catch blocks just to satisfy the compiler.
 */
public class SecSuiteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SecSuiteException(String message) {
        super(message);
    }

    public SecSuiteException(String message, Throwable cause) {
        super(message, cause);
    }
}
