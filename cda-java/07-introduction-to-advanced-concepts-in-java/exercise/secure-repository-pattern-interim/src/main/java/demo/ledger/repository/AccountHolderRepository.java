package demo.ledger.repository;

import demo.ledger.crypto.FieldCipher;
import demo.ledger.domain.AccountHolder;
import demo.ledger.domain.HolderSummary;
// DataAccessException, DuplicateKeyException, RowMapper, ZoneOffset and Pattern are
// imported for the TODO steps. IntelliJ shows them greyed out until you use them.
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
 * <p><b>This is the starting point.</b> Every method works, the way a quick
 * first draft often does: SQL built by concatenation, nothing validated,
 * {@code SELECT *}, and raw error messages printed to the screen. Follow the
 * TODO steps in order. Run the app before each fix so you see the problem first.
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

    // COMPLETE: Step 1 of 8 — Confirm this class is the ONLY path to account_holders.
    //   - Ctrl+Shift+F (Find in Files) for "account_holders". The only hits should be this
    //     file and schema.sql. A class that builds its own SQL against this table would
    //     bypass every fix below — a repository is a security boundary only if it is the
    //     only way in.
    //   - ConsoleMenu calls the four methods below and catches only IllegalArgumentException
    //     and DataAccessFailure. AccountRepository is a finished example of this same pattern.
    //   - Add the two practice holders from README setup step 5 (menu 4):
    //     ACC-00000001 / Jordan Rivera / 123-45-6789 and ACC-00000003 / Maria Lopez / 555-12-3456.
    //     Menu 1, sorted by display_name, should list both. Steps 3 to 7 use them.

    // COMPLETE: Step 5 of 8 — Add the sort allow-list constant here (see findAll).
    //   - private static final List<String> SORT_COLUMNS =
    //         List.of("account_id", "display_name", "created_at");
    private static final List<String> SORT_COLUMNS = List.of("account_id", "display_name", " created_at");

    // COMPLETE: Step 6 of 8 — Add the boundary rules as java.util.regex.Pattern constants.
    //   - DISPLAY_NAME: "[\\p{L} .'-]{1,128}"   (letters, spaces, apostrophes, periods, hyphens)
    //   - NAME_SEARCH:  "[\\p{L} .'-]{1,64}"
    //   - TAX_ID:       "\\d{3}-\\d{2}-\\d{4}"
    //   - See how AccountRepository.debit uses Validate.amount to stop a -50.00 "debit"
    //     from silently adding money. Same idea here: reject bad input before any SQL runs.
    private final Pattern DISPLAY_NAME = Pattern.compile("[\\p{L} .'-]{1,128}" );
    private final Pattern NAME_SEARCH = Pattern.compile("[\\p{L} .'-]{1,64}");
    private final Pattern TAX_ID = Pattern.compile("\\d{3}-\\d{2}-\\d{4}");


    // COMPLETE: Step 8 of 8 — Add an explicit RowMapper for summaries (safe mapping).
    //   - private static final RowMapper<HolderSummary> SUMMARY = (rs, rowNum) -> new HolderSummary(
    //         rs.getString("account_id"), rs.getString("display_name"),
    //         rs.getObject("created_at", OffsetDateTime.class));
    //   - It replaces .query(HolderSummary.class), which fills a record from whatever
    //     columns SELECT * happens to return.
    private final RowMapper<HolderSummary> SUMMARY = (rs, rowNum) -> new HolderSummary(
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
        // COMPLETE: Step 6 of 8 — Validate every input before any SQL runs.
        //   - Validate.accountId(accountId);
        //   - Validate.matches(displayName, DISPLAY_NAME, "Name must be 1-128 letters, spaces, apostrophes, periods or hyphens");
        //   - Validate.matches(taxId, TAX_ID, "Tax ID must look like 123-45-6789");
        Validate.accountId(accountId);
        Validate.matches(displayName, DISPLAY_NAME, "Name must be 1-128 letters, spaces, apostrophes, periods or hyphens");
        Validate.matches(taxId, TAX_ID, "Tax ID must look like 123-45-6789");

        // COMPLETE: Step 2 of 8 — Parameterize the INSERT.
        //   - Run it first: menu 4, add "Pat O'Brien" to ACC-00000002. The apostrophe alone breaks the SQL.
        //   - Replace every concatenated value with a named parameter
        //     (:accountId, :displayName, :taxIdEncrypted, :createdAt) and bind each
        //     one with .param("name", value) before .update().
        //   - Bind createdAt as OffsetDateTime.now(ZoneOffset.UTC), not as a string.
        //   - Try "Pat O'Brien" again: the apostrophe is now just data.

        try {

            jdbc.sql("""
                    INSERT INTO account_holders (
                    account_id, display_name, tax_id_encrypted, created_at) 
                    VALUES (:accountId, :displayName, :taxEncrypted, :createdAt)""")
                    .param("accountId", accountId)
                    .param("displayName",displayName)
                    .param("taxEncrypted",cipher.encryptField(taxId))
                    .param("createdAt", OffsetDateTime.now(ZoneOffset.UTC))
                    .update();

        } catch (DuplicateKeyException ex) {
            throw DataAccessFailure.logged("create account holder", "That Account Already Has A Holder.", ex);
            // COMPLETE: Step 7 of 8 — Handle exceptions securely ("log rich, respond thin").
            //   - Look at what Step 2's failure printed: our SQL, on screen. Then the menu
            //     said "Holder saved." anyway, because this block swallowed the failure.
            //   - Catch org.springframework.dao.DataAccessException (not Exception) and
            //     throw DataAccessFailure.logged(operation, safeMessage, ex) — full detail
            //     goes to logs/ledger-demo.log, a generic message + ref goes to the caller.
            //   - Here, catch DuplicateKeyException FIRST with its own safe message:
            //     "That account already has a holder." Then DataAccessException with
            //     "Could not save the account holder."
        } catch (DataAccessException ex){
            throw DataAccessFailure.logged("create account holder", "Could Not Create Account Holder", ex);
        }
    }

    public List<HolderSummary> searchByName(String namePart) {
        // COMPLETE: Step 6 of 8 — Validate.matches(namePart, NAME_SEARCH,
        //   "Search text must be 1-64 letters, spaces, apostrophes, periods or hyphens");
        Validate.matches(namePart, NAME_SEARCH, "Search text must be 1-64 letters, spaces, apostrophes, periods or hyphens");
        // COMPLETE: Step 3 of 8 — Parameterize the search.
        //   - Run it first: menu 2, search for   zzz' OR 1=1 --   and every holder comes back.
        //   - Use WHERE display_name ILIKE :pattern and bind .param("pattern", "%" + namePart + "%").
        //     The % wildcards belong in the bound VALUE, never in the SQL text.
        // COMPLETE: Step 8 of 8 — SELECT account_id, display_name, created_at (not *), and
        //   .query(SUMMARY) instead of .query(HolderSummary.class).
//        String sql = "SELECT * FROM account_holders WHERE display_name ILIKE '%" + namePart + "%' ORDER BY display_name";
        try {
            return jdbc.sql("""
                            SELECT account_id, display_name, created_at
                            FROM account_holders
                            WHERE display_name ILIKE :pattern
                            ORDER BY display_name
                            """
                    ).param("pattern", "%" + namePart + "%")
                    .query(SUMMARY).list();
        } catch (DataAccessException ex) {
            // COMLETE: Step 7 of 8 — DataAccessException -> DataAccessFailure.logged("search account holders",
            //   "Could not search account holders.", ex). Returning an empty list hid the failure.
            throw DataAccessFailure.logged("search account holders", "Could Not Search Account Holders", ex);
        }
    }

    /** The only method that returns the tax ID, and it decrypts here, at the boundary. */
    public Optional<AccountHolder> findById(String accountId) {
        // TODO: Step 6 of 8 — Validate.accountId(accountId);

        // TODO: Step 4 of 8 — Parameterize the lookup: WHERE account_id = :accountId.
        //   - Run it first: menu 3 with   ' OR 1=1 LIMIT 1 --   shows a holder you never asked for.
        // TODO: Step 8 of 8 — SELECT exactly the four columns this record needs:
        //   account_id, display_name, tax_id_encrypted, created_at.
        String sql = "SELECT * FROM account_holders WHERE account_id = '" + accountId + "'";
        try {
            return jdbc.sql(sql)
                    .query((rs, rowNum) -> new AccountHolder(
                            rs.getString("account_id"),
                            rs.getString("display_name"),
                            cipher.decryptField(rs.getString("tax_id_encrypted")),
                            rs.getObject("created_at", OffsetDateTime.class)))
                    .optional();
        } catch (Exception ex) {
            // TODO: Step 7 of 8 — Catch DataAccessException | IllegalStateException (FieldCipher throws
            //   the latter for a wrong key or a tampered or never-encrypted value) and throw
            //   DataAccessFailure.logged("load account holder", "Could not load that account holder.", ex).
            System.out.println("Database error: " + ex.getMessage());
            return Optional.empty();
        }
    }

    public List<HolderSummary> findAll(String sortColumn) {
        // TODO: Step 5 of 8 — Allow-list the sort column.
        //   - Run it first: menu 1, sort by   1/0   and Postgres runs our text ("division by zero").
        //   - JDBC can bind values, not identifiers: ORDER BY :sortColumn does not work.
        //   - If SORT_COLUMNS does not contain sortColumn, throw
        //     new IllegalArgumentException("Sort column must be one of " + SORT_COLUMNS).
        //   - Only after that check is concatenating sortColumn into ORDER BY safe.
        // TODO: Step 8 of 8 — SELECT account_id, display_name, created_at (not *), and .query(SUMMARY).
        String sql = "SELECT * FROM account_holders ORDER BY " + sortColumn;
        try {
            return jdbc.sql(sql).query(HolderSummary.class).list();
        } catch (Exception ex) {
            // TODO: Step 7 of 8 — DataAccessException -> DataAccessFailure.logged("list account holders",
            //   "Could not load account holders.", ex).
            System.out.println("Database error: " + ex.getMessage());
            return List.of();
        }
    }
}
