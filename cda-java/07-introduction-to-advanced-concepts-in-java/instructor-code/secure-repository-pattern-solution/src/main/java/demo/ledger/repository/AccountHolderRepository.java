package demo.ledger.repository;

import demo.ledger.crypto.FieldCipher;
import demo.ledger.domain.AccountHolder;
import demo.ledger.domain.HolderSummary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The only path to {@code account_holders} — the repository this code-along builds.
 *
 * <p><b>This is the completed solution.</b> Every {@code TODO: (Completed) Step N of 8}
 * comment shows the original step instruction next to the code that finishes it.
 *
 * <p>Code-along order (matches the Instructor Guide's segments):
 * <pre>
 *   Step 1      The repository as a security boundary        (top of class)
 *   Steps 2-5   Parameterized access and the sort allow-list (create, searchByName, findById, findAll)
 *   Step 6      Boundary input validation                    (constants + every method)
 *   Steps 7-8   Secure exceptions and safe result mapping    (every catch block, every query)
 * </pre>
 */
@Repository
public class AccountHolderRepository {

    // TODO: (Completed) Step 1 of 8 — Confirm this class is the ONLY path to account_holders.
    //   - Ctrl+Shift+F (Find in Files) for "account_holders". The only hits are this file
    //     and schema.sql. A class that builds its own SQL against this table would bypass
    //     every fix below — a repository is a security boundary only if it is the only way in.
    //   - ConsoleMenu calls the four methods below and catches only IllegalArgumentException
    //     and DataAccessFailure. AccountRepository is a finished example of this same pattern.
    //   - Add the two practice holders from README setup step 5 (menu 4):
    //     ACC-00000001 / Jordan Rivera / 123-45-6789 and ACC-00000003 / Maria Lopez / 555-12-3456.
    //     Menu 1, sorted by display_name, should list both. Steps 3 to 7 use them.

    // TODO: (Completed) Step 5 of 8 — Allow-list the sort column.
    /** JDBC can bind values, not identifiers — so a sort column is checked against this list instead. */
    private static final List<String> SORT_COLUMNS = List.of("account_id", "display_name", "created_at");

    // TODO: (Completed) Step 6 of 8 — Add the boundary rules as constants.
    //   - DISPLAY_NAME: 1-128 letters, spaces, apostrophes, periods, hyphens.
    //   - NAME_SEARCH:  the same characters, 1-64 long.
    //   - TAX_ID:       ddd-dd-dddd.
    private static final Pattern DISPLAY_NAME = Pattern.compile("[\\p{L} .'-]{1,128}");
    private static final Pattern NAME_SEARCH = Pattern.compile("[\\p{L} .'-]{1,64}");
    private static final Pattern TAX_ID = Pattern.compile("\\d{3}-\\d{2}-\\d{4}");

    // TODO: (Completed) Step 8 of 8 — Map results explicitly (safe mapping).
    //   - SUMMARY reads exactly three columns and nothing else. It replaces
    //     .query(HolderSummary.class), which fills a record from whatever columns
    //     SELECT * happens to return.
    private static final RowMapper<HolderSummary> SUMMARY = (rs, rowNum) -> new HolderSummary(
            rs.getString("account_id"),
            rs.getString("display_name"),
            rs.getObject("created_at", OffsetDateTime.class));

    private final JdbcClient jdbc;
    private final FieldCipher cipher;

    public AccountHolderRepository(JdbcClient jdbc, FieldCipher cipher) {
        this.jdbc = jdbc;
        this.cipher = cipher;
    }

    public void create(String accountId, String displayName, String taxId) {
        // TODO: (Completed) Step 6 of 8 — Validate every input before any SQL runs.
        //   - Validate.accountId(...), then Validate.matches(...) with DISPLAY_NAME and TAX_ID.
        Validate.accountId(accountId);
        Validate.matches(displayName, DISPLAY_NAME, "Name must be 1-128 letters, spaces, apostrophes, periods or hyphens");
        Validate.matches(taxId, TAX_ID, "Tax ID must look like 123-45-6789");
        try {
            // TODO: (Completed) Step 2 of 8 — Parameterize the INSERT.
            //   - Menu 4 with "Pat O'Brien" broke the concatenated SQL on the apostrophe alone.
            //   - Every value is now a named parameter bound with .param(...);
            //     createdAt is bound as an OffsetDateTime, not a string.
            jdbc.sql("""
                            INSERT INTO account_holders (account_id, display_name, tax_id_encrypted, created_at)
                            VALUES (:accountId, :displayName, :taxIdEncrypted, :createdAt)""")
                    .param("accountId", accountId)
                    .param("displayName", displayName)
                    .param("taxIdEncrypted", cipher.encryptField(taxId))
                    .param("createdAt", OffsetDateTime.now(ZoneOffset.UTC))
                    .update();
        // TODO: (Completed) Step 7 of 8 — Handle exceptions securely ("log rich, respond thin").
        //   - catch (Exception) + printing ex.getMessage() showed SQL on screen, then swallowed
        //     the failure so the menu still said "Holder saved."
        //   - Catch Spring's DataAccessException and throw DataAccessFailure.logged(operation,
        //     safeMessage, ex): full detail to the log, a generic message + ref to the caller.
        //   - A known, safe-to-explain case (DuplicateKeyException) gets its own message first.
        } catch (DuplicateKeyException ex) {
            throw DataAccessFailure.logged("create account holder", "That account already has a holder.", ex);
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("create account holder", "Could not save the account holder.", ex);
        }
    }

    public List<HolderSummary> searchByName(String namePart) {
        // TODO: (Completed) Step 6 of 8 — Validate the search text with NAME_SEARCH.
        Validate.matches(namePart, NAME_SEARCH, "Search text must be 1-64 letters, spaces, apostrophes, periods or hyphens");
        try {
            // TODO: (Completed) Step 3 of 8 — Parameterize the search.
            //   - x' OR '1'='1 returned every holder. Now the SQL says ILIKE :pattern and the
            //     % wildcards live in the bound VALUE, never in the SQL text.
            // TODO: (Completed) Step 8 of 8 — Name the columns and map with SUMMARY.
            return jdbc.sql("""
                            SELECT account_id, display_name, created_at
                            FROM account_holders
                            WHERE display_name ILIKE :pattern
                            ORDER BY display_name""")
                    .param("pattern", "%" + namePart + "%")
                    .query(SUMMARY)
                    .list();
        // TODO: (Completed) Step 7 of 8 — DataAccessException -> DataAccessFailure.logged(...).
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("search account holders", "Could not search account holders.", ex);
        }
    }

    /** The only method that returns the tax ID, and it decrypts here, at the boundary. */
    public Optional<AccountHolder> findById(String accountId) {
        // TODO: (Completed) Step 6 of 8 — Validate.accountId(...).
        Validate.accountId(accountId);
        try {
            // TODO: (Completed) Step 4 of 8 — Parameterize the lookup with :accountId.
            // TODO: (Completed) Step 8 of 8 — Name the four columns this record needs.
            return jdbc.sql("""
                            SELECT account_id, display_name, tax_id_encrypted, created_at
                            FROM account_holders
                            WHERE account_id = :accountId""")
                    .param("accountId", accountId)
                    .query((rs, rowNum) -> new AccountHolder(
                            rs.getString("account_id"),
                            rs.getString("display_name"),
                            cipher.decryptField(rs.getString("tax_id_encrypted")),
                            rs.getObject("created_at", OffsetDateTime.class)))
                    .optional();
        // TODO: (Completed) Step 7 of 8 — Also catch IllegalStateException: FieldCipher throws it
        //   for a wrong key or a tampered or never-encrypted value.
        } catch (DataAccessException | IllegalStateException ex) {
            throw DataAccessFailure.logged("load account holder", "Could not load that account holder.", ex);
        }
    }

    public List<HolderSummary> findAll(String sortColumn) {
        // TODO: (Completed) Step 5 of 8 — Allow-list the sort column.
        //   - Sorting by 1/0 ran our text as SQL ("division by zero").
        //   - ORDER BY :sortColumn cannot work, so the check below runs first.
        if (!SORT_COLUMNS.contains(sortColumn)) {
            throw new IllegalArgumentException("Sort column must be one of " + SORT_COLUMNS);
        }
        try {
            // Concatenation is safe here ONLY because sortColumn is now one of three known strings.
            // TODO: (Completed) Step 8 of 8 — Name the columns and map with SUMMARY.
            return jdbc.sql("SELECT account_id, display_name, created_at FROM account_holders ORDER BY " + sortColumn)
                    .query(SUMMARY)
                    .list();
        // TODO: (Completed) Step 7 of 8 — DataAccessException -> DataAccessFailure.logged(...).
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("list account holders", "Could not load account holders.", ex);
        }
    }
}
