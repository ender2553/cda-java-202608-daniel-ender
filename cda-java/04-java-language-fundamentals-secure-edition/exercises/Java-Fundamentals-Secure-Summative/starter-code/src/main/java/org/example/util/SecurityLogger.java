package org.example.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * USER STORY: As a security analyst, when something goes wrong I need a
 * SAFE ERROR SURFACE: the console (and, eventually, the executive report)
 * should only ever show a short, generic message -- never a raw exception
 * message, a stack trace, a file path, or a class name. Anyone watching
 * the console over an analyst's shoulder, or reading a report that gets
 * emailed around, should learn nothing about the internals of this
 * application from an error. Full diagnostic detail still needs to go
 * SOMEWHERE so the analyst can actually fix the problem -- it goes to a
 * server-side log file instead.
 *
 * ACCEPTANCE CRITERIA:
 *  1. logDetailed(context, throwable) appends a timestamped entry
 *     containing the context string, the exception's class name, message,
 *     and full stack trace to logs/security-server.log. It creates the
 *     logs/ directory and the file if they do not already exist.
 *  2. logDetailed(...) uses try-with-resources for the file handle, so the
 *     handle is always released even if writing throws partway through.
 *  3. logDetailed(...) never throws a checked exception to its caller --
 *     if logging itself fails (e.g., disk full), it must not crash the
 *     calling code. In that fallback case, print only a short warning to
 *     stderr; do NOT print the original exception's message or trace to
 *     stderr, since stderr may be visible to the end user (that would
 *     defeat the whole point of this class).
 *  4. toUserMessage(context) returns a short, generic, non-identifying
 *     message plus a reference code the analyst can hand to support (or
 *     grep for in the log file) to correlate the two. It must never
 *     include the exception's message, class name, or stack trace.
 *  5. Every call to logDetailed(...) and the matching toUserMessage(...)
 *     for the SAME incident should share the same reference code, so an
 *     analyst can find the detailed entry from the generic message.
 */
public final class SecurityLogger {

    private static final Path LOG_DIR = Paths.get("logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("security-server.log");

    private SecurityLogger() { }

    /**
     * Logs full diagnostic detail server-side and returns a short
     * reference code correlating this log entry to a user-facing message
     * produced by {@link #userMessageFor(String, String)}.
     */
    public static String logDetailed(String context, Throwable t) {
        String referenceCode = generateReferenceCode();
        try {
            Files.createDirectories(LOG_DIR);
            // try-with-resources: the Writer is guaranteed to be closed
            // even if an exception is thrown while writing the trace.
            try (Writer fileWriter = Files.newBufferedWriter(
                        LOG_FILE, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                 PrintWriter out = new PrintWriter(fileWriter)) {

                out.println("---------------------------------------------------------------");
                out.println("Timestamp:  " + Instant.now());
                out.println("Reference:  " + referenceCode);
                out.println("Context:    " + context);
                if (t != null) {
                    out.println("Exception:  " + t.getClass().getName() + ": " + t.getMessage());
                    t.printStackTrace(out);
                } else {
                    out.println("Exception:  (none)");
                }
                out.println();
            }
        } catch (IOException loggingFailure) {
            // Fail-safe: logging itself failed. Do NOT crash the caller,
            // and do NOT leak the original exception's details to stderr.
            System.err.println("[SecurityLogger] WARNING: unable to write security log " +
                    "(reference " + referenceCode + "). Contact an administrator.");
        }
        return referenceCode;
    }

    /** Generic, non-identifying message for end users, tagged with a correlation code. */
    public static String userMessageFor(String genericSummary, String referenceCode) {
        return genericSummary + " (Reference: " + referenceCode + ". See server logs for detail.)";
    }

    private static String generateReferenceCode() {
        // Short, human-copyable code; not a security token, just a log correlator.
        long ticks = System.nanoTime();
        return "ERR-" + Long.toHexString(ticks).toUpperCase();
    }
}
