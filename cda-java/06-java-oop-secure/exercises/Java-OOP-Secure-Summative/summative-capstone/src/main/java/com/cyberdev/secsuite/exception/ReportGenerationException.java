package com.cyberdev.secsuite.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Thrown by ReportService.generateMarkdownReport (SEC-16) when the Markdown report cannot be
 * written (output directory cannot be created, disk full, permission denied, ...). Wraps the
 * underlying IOException so callers never need to know about java.io.
 */
public class ReportGenerationException extends SecSuiteException {

    private static final long serialVersionUID = 1L;

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
