package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 6
 * Secure Alert Intake Toolkit
 * FILE 2 of 7: InvalidAlertException.java
 * ============================================================
 *
 * TODO 1: Write a class InvalidAlertException that extends
 * RuntimeException (unchecked — a rejected alert line is a caller
 * error, not a checked, recoverable system condition).
 *
 * TODO 2: Give it TWO constructors:
 *   - one taking just a String message (calls super(message))
 *   - one taking a String message AND a Throwable cause (calls
 *     super(message, cause)) — this second one lets you WRAP a
 *     lower-level exception (like NumberFormatException) while
 *     keeping your own exception's message clean and specific. The
 *     original exception is preserved as the "cause" for anyone who
 *     needs the full detail (e.g., an internal log), without exposing
 *     it in the message shown elsewhere.
 * ============================================================
 */
public class InvalidAlertException extends RuntimeException {

    // TODO 1 and 2: write both constructors here.

}
