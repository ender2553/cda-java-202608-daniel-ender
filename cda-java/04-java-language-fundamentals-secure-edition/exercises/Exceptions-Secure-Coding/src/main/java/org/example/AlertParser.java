package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 6
 * Secure Alert Intake Toolkit
 * FILE 5 of 7: AlertParser.java
 * ============================================================
 *
 * This is the heart of the lab. Every check below must be FAIL-CLOSED:
 * on any problem, throw InvalidAlertException immediately with a
 * specific, clear message. NEVER coerce, default, or silently "fix"
 * a bad value. Run checks in this order — cheapest first, before any
 * regex or numeric work ever touches the value.
 *
 * Expected raw line format: "cveId,component,severity,score"
 * e.g., "CVE-2024-1234,openssl,HIGH,7.5"
 *
 * TODO 1: Declare two constants: MAX_LINE_LENGTH (use 200) and
 * MAX_COMPONENT_LENGTH (use 50).
 *
 * TODO 2: Declare a compiled Pattern for CVE ID format. Use exactly:
 *   "^CVE-\\d{4}-\\d{4,}$"
 * Notice the ^ and $ anchors — this is the EXACT lesson from this
 * morning's Predict-Then-Check. Without them, a line like
 * "JUNK-CVE-2024-1234-JUNK" would incorrectly pass validation, since
 * the valid-looking substring is still "in there" somewhere.
 *
 * TODO 3: Write parseAlertLine(String rawLine) that:
 *   a) LENGTH: throws InvalidAlertException if rawLine is null or
 *      longer than MAX_LINE_LENGTH.
 *   b) FORMAT: splits rawLine on "," and throws if the result doesn't
 *      have exactly 4 parts.
 *   c) FORMAT: trims parts[0] as cveId; throws if it doesn't fully
 *      match your anchored CVE_PATTERN (use .matches(), not .find()).
 *   d) TYPE/LENGTH: trims parts[1] as component; throws if it's empty
 *      or longer than MAX_COMPONENT_LENGTH.
 *   e) ALLOW-LIST: trims and uppercases parts[2], then calls
 *      Severity.valueOf(...) inside a try/catch. If it throws
 *      IllegalArgumentException, catch it and throw a NEW
 *      InvalidAlertException, passing the original exception as the
 *      cause (use your two-argument constructor from
 *      InvalidAlertException.java).
 *   f) TYPE/RANGE: trims parts[3] as scoreRaw, parses it with
 *      Double.parseDouble() inside a try/catch for NumberFormatException
 *      (wrap it the same way as step e). Then check the parsed value
 *      is between 0.0 and 10.0 inclusive — throw (WITHOUT wrapping,
 *      since there's no lower-level exception here) if it's out of range.
 *   g) If everything passed, return a new Alert built from the four
 *      validated values.
 * ============================================================
 */
import java.util.regex.Pattern;

public class AlertParser {

    // TODO 1: declare your two length constants here.


    // TODO 2: declare your anchored CVE_PATTERN here.


    // TODO 3: write parseAlertLine(String rawLine) here.


}
