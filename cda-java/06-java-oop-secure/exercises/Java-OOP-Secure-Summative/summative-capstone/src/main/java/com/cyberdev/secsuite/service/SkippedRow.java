package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ValidationException;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One CSV row that SEC-10 refused to ingest, and why. lineNumber is the 1-based physical line
 * in the file (the header is line 1, so the first data row is line 2) -- the number an analyst
 * would see in a text editor. externalAlertId is whatever the row claimed its id was, if that
 * field could be read at all (may be null or blank for rows too broken to tell).
 *
 * SECURITY CALLOUT: reason is built by OUR code from a fixed vocabulary plus (bounded,
 * already-validated-for-control-characters) fragments of the offending value; the report
 * escapes it again before rendering, because it can still contain feed text.
 */
public record SkippedRow(int lineNumber, String externalAlertId, String reason) {

    public SkippedRow {
        if (lineNumber < 1) {
            throw new ValidationException("lineNumber must be >= 1, was " + lineNumber);
        }
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("reason must not be blank");
        }
    }
}
