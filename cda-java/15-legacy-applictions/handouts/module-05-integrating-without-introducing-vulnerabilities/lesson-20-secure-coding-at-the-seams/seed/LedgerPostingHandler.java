package seam.exercise;

import java.util.Map;

/**
 * Seed for "Harden a Seam — Validate Both Directions."
 *
 * This handler is deliberately flawed. Rewrite it; do not run it against a real ledger.
 *
 * The new service calls post(...) with the raw request fields it received:
 *   accountId  the account to post against, e.g. "acct-1001"
 *   delta      the amount in whole cents, as text, e.g. "-2500"
 *   memo       free text shown on the statement
 *   kind       the posting type; the legacy core branches on it ("DEBIT", "CREDIT", "HOLD")
 *
 * The handler returns whatever the legacy core hands back to the new service.
 */
public class LedgerPostingHandler {

    /** The legacy core's raw SQL entry point. Returns the affected row, or null. */
    public interface LegacyCore {
        Map<String, Object> run(String sql);
    }

    private final LegacyCore legacy;

    public LedgerPostingHandler(LegacyCore legacy) {
        this.legacy = legacy;
    }

    public Map<String, Object> post(String accountId, String delta, String memo, String kind) {
        String sql = "INSERT INTO ledger (account_id, delta, memo, kind) VALUES ('"
                + accountId + "', " + delta + ", '" + memo + "', '" + kind + "')";
        try {
            Map<String, Object> row = legacy.run(sql);
            legacy.run("UPDATE accounts SET balance = balance + " + delta
                    + " WHERE id = '" + accountId + "'");
            return row;
        } catch (RuntimeException e) {
            return Map.of("error", e.toString(), "sql", sql);
        }
    }
}
