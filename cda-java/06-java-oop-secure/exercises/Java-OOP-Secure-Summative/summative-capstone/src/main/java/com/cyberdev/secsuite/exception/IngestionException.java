package com.cyberdev.secsuite.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Thrown by ThreatIntelCsvIngestionService (SEC-10) for FILE-LEVEL failures only: the file
 * is missing or unreadable, too large, or its header row is not the expected header. A
 * single malformed ROW is never an IngestionException -- it is recorded as a SkippedRow and
 * ingestion continues with the next row. Wrapping the underlying IOException here keeps
 * java.io details from leaking out of the service's public contract.
 */
public class IngestionException extends SecSuiteException {

    private static final long serialVersionUID = 1L;

    public IngestionException(String message) {
        super(message);
    }

    public IngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
