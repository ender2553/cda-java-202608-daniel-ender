package demo.clinic.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * The only exception that leaves the data layer when the database or a file
 * fails. The full cause goes to the log; the caller gets a generic message plus
 * a short reference to search the log for.
 */
public class DataAccessFailure extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(DataAccessFailure.class);

    private DataAccessFailure(String safeMessage) {
        super(safeMessage);
    }

    public static DataAccessFailure logged(String operation, String safeMessage, Exception cause) {
        String ref = UUID.randomUUID().toString().substring(0, 8);
        log.error("[ref {}] Failed during '{}'", ref, operation, cause);
        return new DataAccessFailure(safeMessage + " (ref " + ref + ")");
    }
}
