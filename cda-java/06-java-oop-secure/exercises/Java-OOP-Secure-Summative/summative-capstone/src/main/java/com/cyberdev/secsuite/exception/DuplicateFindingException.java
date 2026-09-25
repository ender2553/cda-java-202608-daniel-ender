package com.cyberdev.secsuite.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Thrown by ScannerService.recordFinding (SEC-4) when an OPEN scan finding already exists
 * for the same asset + CVE. Mirrors QuickPay POS's DuplicateTransactionException: a
 * specific, catchable type for one specific fail-closed refusal, rather than a generic
 * IllegalStateException the caller cannot distinguish from a bug.
 */
public class DuplicateFindingException extends SecSuiteException {

    private static final long serialVersionUID = 1L;

    private final String hostname;
    private final String cveId;

    public DuplicateFindingException(String hostname, String cveId) {
        super("An OPEN finding for " + cveId + " on " + hostname + " is already recorded");
        this.hostname = hostname;
        this.cveId = cveId;
    }

    public String getHostname() {
        return hostname;
    }

    public String getCveId() {
        return cveId;
    }
}
