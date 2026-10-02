package demo.ledger.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * The only exception a repository lets escape when the database fails.
 * "Log rich, respond thin": the full cause (SQL, table, constraint, driver
 * detail) goes to the log file; the caller gets a generic message plus a
 * short reference that support can search the log for.
 *
 * <p>The cause is deliberately NOT attached, so no caller can print
 * {@code getCause()} and leak it anyway.
 */
public class DataAccessFailure extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(DataAccessFailure.class);

    private DataAccessFailure(String safeMessage) {
        super(safeMessage);
    }

    static DataAccessFailure logged(String operation, String safeMessage, Exception cause) {
        String ref = UUID.randomUUID().toString().substring(0, 8);
        log.error("[ref {}] Data access failed during '{}'", ref, operation, cause);
        return new DataAccessFailure(safeMessage + " (ref " + ref + ")");
    }
}
