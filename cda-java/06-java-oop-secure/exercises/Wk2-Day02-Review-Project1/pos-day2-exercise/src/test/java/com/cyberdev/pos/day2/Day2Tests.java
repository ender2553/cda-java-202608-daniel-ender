package com.cyberdev.pos.day2;

import com.cyberdev.pos.config.DatabaseConfig;
import com.cyberdev.pos.day1.Cart;
import com.cyberdev.pos.day1.LineItem;
import com.cyberdev.pos.day1.Product;
import com.cyberdev.pos.day2.inmemory.InMemoryTransactionRepository;
import com.cyberdev.pos.day2.jdbc.JdbcMerchantRepository;
import com.cyberdev.pos.day2.jdbc.JdbcTransactionRepository;
import com.cyberdev.pos.day2.jdbc.mapper.TransactionRecordRowMapper;
import com.cyberdev.pos.day2.support.FakeResultSet;
import com.cyberdev.pos.exception.DuplicateTransactionException;
import com.cyberdev.pos.testkit.GradedTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * Two kinds of tests live here now that persistence is REAL PostgreSQL (see
 * schema/schema.sql) instead of the old SimulatedDatabase:
 *
 *  - Tests of JdbcTransactionRepository/JdbcMerchantRepository (POS2-1, 2-2, 2-3, 2-4, and
 *    the second half of 2-8) actually talk to Postgres via JdbcTemplate. They need a live
 *    database reachable at POS_DB_URL/POS_DB_USER/POS_DB_PASSWORD (see README) with
 *    schema/schema.sql already applied. If no database is reachable, these tests are SKIPPED
 *    (via JUnit's Assumptions), not failed -- a missing dev database is an environment
 *    problem, not a code problem, and a hard failure here would be misleading either way.
 *  - Tests of TransactionService (POS2-5, 2-6, 2-7), Merchant (POS2-9), and the RowMapper
 *    mapping logic itself (the first half of POS2-8) do NOT need a database at all: the
 *    service tests run against InMemoryTransactionRepository (proving TransactionService
 *    only depends on the TransactionRepository INTERFACE, never a concrete implementation),
 *    and the RowMapper test uses FakeResultSet, a lightweight in-memory java.sql.ResultSet
 *    stand-in (see support/FakeResultSet.java).
 */
public class Day2Tests {

    private static JdbcTemplate jdbcTemplate;
    private static boolean dbAvailable;

    @BeforeAll
    static void connectToDatabase() {
        DataSource dataSource = DatabaseConfig.createDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbAvailable = true;
        } catch (Exception e) {
            dbAvailable = false;
        }
    }

    @BeforeEach
    void cleanTables() {
        if (dbAvailable) {
            jdbcTemplate.update("DELETE FROM transaction");
            jdbcTemplate.update("DELETE FROM merchant");
        }
    }

    private void assumeDbAvailable() {
        Assumptions.assumeTrue(dbAvailable,
                "No PostgreSQL database reachable at POS_DB_URL (see README/schema/schema.sql) -- skipping JDBC-backed test");
    }

    // ---------------------------------------------------------------
    // POS2-1: JdbcTransactionRepository.save() -- parameterized INSERT
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "POS2-1", points = 6, description = "save() persists via a parameterized INSERT and findById can read it back")
    public void repository_saveAndFindById() {
        assumeDbAvailable();
        JdbcTransactionRepository repo = new JdbcTransactionRepository(jdbcTemplate);
        TransactionRecord record = new TransactionRecord("TXN-1", "M1", new BigDecimal("19.99"), "coffee", java.time.Instant.now());
        repo.save(record);
        Optional<TransactionRecord> found = repo.findById("TXN-1");
        assertTrue(found.isPresent(), "saved record should be findable");
        assertEquals("TXN-1", found.get().getTransactionId());
    }

    @Test
    @GradedTest(tag = "POS2-1", points = 3, description = "save() does not corrupt data when memo contains quote characters (proves no unsafe string building)")
    public void repository_saveHandlesQuoteCharactersSafely() {
        assumeDbAvailable();
        JdbcTransactionRepository repo = new JdbcTransactionRepository(jdbcTemplate);
        TransactionRecord record = new TransactionRecord("TXN-Q", "M1", new BigDecimal("5.00"), "o'brien's order", java.time.Instant.now());
        assertDoesNotThrow(() -> repo.save(record), "saving a memo containing a quote must not throw or corrupt state");
        Optional<TransactionRecord> found = repo.findById("TXN-Q");
        assertTrue(found.isPresent());
        assertEquals("o'brien's order", found.get().getMemo(), "memo must be stored exactly, not mangled");
    }

    // ---------------------------------------------------------------
    // POS2-2: JdbcTransactionRepository.findById() -- parameterized lookup
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "POS2-2", points = 7, description = "findById returns empty for a non-existent id, does not match unrelated rows")
    public void repository_findByIdMissing() {
        assumeDbAvailable();
        JdbcTransactionRepository repo = new JdbcTransactionRepository(jdbcTemplate);
        repo.save(new TransactionRecord("TXN-1", "M1", new BigDecimal("1.00"), "x", java.time.Instant.now()));
        assertTrue(repo.findById("TXN-DOES-NOT-EXIST").isEmpty(), "unknown id must return empty, not throw or match everything");
    }

    // ---------------------------------------------------------------
    // POS2-3: JdbcTransactionRepository.searchByMemo() -- SQL injection fix
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "POS2-3", points = 9, description = "searchByMemo is not vulnerable to SQL-injection-style payloads")
    public void repository_searchByMemoResistsInjection() {
        assumeDbAvailable();
        JdbcTransactionRepository repo = new JdbcTransactionRepository(jdbcTemplate);
        repo.save(new TransactionRecord("TXN-1", "M1", new BigDecimal("1.00"), "latte", java.time.Instant.now()));
        repo.save(new TransactionRecord("TXN-2", "M1", new BigDecimal("2.00"), "espresso", java.time.Instant.now()));

        List<TransactionRecord> injectionAttempt = repo.searchByMemo("nonexistent' OR '1'='1");
        assertTrue(injectionAttempt.isEmpty(),
                "a classic SQL-injection payload must not widen the match to every row -- searchByMemo must use a parameterized query");
    }

    @Test
    @GradedTest(tag = "POS2-3", points = 5, description = "searchByMemo still finds legitimate substring matches")
    public void repository_searchByMemoNormalUse() {
        assumeDbAvailable();
        JdbcTransactionRepository repo = new JdbcTransactionRepository(jdbcTemplate);
        repo.save(new TransactionRecord("TXN-1", "M1", new BigDecimal("1.00"), "large latte", java.time.Instant.now()));
        repo.save(new TransactionRecord("TXN-2", "M1", new BigDecimal("2.00"), "espresso", java.time.Instant.now()));

        List<TransactionRecord> matches = repo.searchByMemo("latte");
        assertEquals(1, matches.size(), "should find exactly one matching memo");
        assertEquals("TXN-1", matches.get(0).getTransactionId());
    }

    // ---------------------------------------------------------------
    // POS2-4: JdbcMerchantRepository.findByMerchantId() -- parameterized lookup
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "POS2-4", points = 6, description = "MerchantRepository.findByMerchantId parameterized lookup")
    public void merchantRepository_findByMerchantId() {
        assumeDbAvailable();
        JdbcMerchantRepository repo = new JdbcMerchantRepository(jdbcTemplate);
        repo.register(new Merchant("M1", "Corner Store"));
        Optional<Merchant> found = repo.findByMerchantId("M1");
        assertTrue(found.isPresent());
        assertEquals("Corner Store", found.get().getDisplayName());
        assertTrue(repo.findByMerchantId("M-NOPE").isEmpty(), "unknown merchantId must return empty");
    }

    // ---------------------------------------------------------------
    // POS2-5/6/7: TransactionService -- DI, cart-total build, duplicate rejection.
    // These are service-layer/logic tests: they run against InMemoryTransactionRepository
    // and need no database, since the point is TransactionService's behavior against
    // WHATEVER TransactionRepository it is given, not the SQL layer itself.
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "POS2-5", points = 8, description = "TransactionService only accepts its repository via the constructor (DI), field is interface-typed")
    public void transactionService_constructorInjection() throws Exception {
        Constructor<?>[] ctors = TransactionService.class.getDeclaredConstructors();
        boolean hasRepositoryConstructor = false;
        for (Constructor<?> ctor : ctors) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length >= 1 && TransactionRepository.class.isAssignableFrom(params[0])) {
                hasRepositoryConstructor = true;
            }
        }
        assertTrue(hasRepositoryConstructor, "TransactionService must have a constructor accepting a TransactionRepository");

        // Prove the service actually USES the injected repository (not an internally-created one)
        TransactionRepository repo = new InMemoryTransactionRepository();
        TransactionService service = new TransactionService(repo);
        Cart cart = new Cart();
        cart.addItem(new LineItem(new Product("P1", "Widget", new BigDecimal("10.00")), 1));
        service.recordSale(cart, "M1", "TXN-DI", "test");
        assertTrue(repo.findById("TXN-DI").isPresent(), "sale recorded via the service must be visible through the SAME injected repository instance");
    }

    @Test
    @GradedTest(tag = "POS2-6", points = 6, description = "recordSale builds a TransactionRecord from the cart total and saves it")
    public void transactionService_recordSaleBuildsRecordFromCartTotal() {
        TransactionRepository repo = new InMemoryTransactionRepository();
        TransactionService service = new TransactionService(repo);

        Cart cart = new Cart();
        cart.addItem(new LineItem(new Product("P1", "Widget", new BigDecimal("12.50")), 2));
        TransactionRecord record = service.recordSale(cart, "M1", "TXN-100", "sale");

        assertEquals(new BigDecimal("25.00"), record.getAmount(), "recorded amount must equal cart.getTotal()");
        assertEquals("M1", record.getMerchantId());
        assertTrue(repo.findById("TXN-100").isPresent(), "record must actually be persisted");
    }

    @Test
    @GradedTest(tag = "POS2-7", points = 10, description = "recordSale fails closed on a duplicate transaction id and does not overwrite the original")
    public void transactionService_recordSaleRejectsDuplicateId() {
        TransactionRepository repo = new InMemoryTransactionRepository();
        TransactionService service = new TransactionService(repo);

        Cart firstCart = new Cart();
        firstCart.addItem(new LineItem(new Product("P1", "Widget", new BigDecimal("10.00")), 1));
        service.recordSale(firstCart, "M1", "TXN-DUP", "first");

        Cart secondCart = new Cart();
        secondCart.addItem(new LineItem(new Product("P2", "Other", new BigDecimal("99.00")), 1));

        assertThrows(DuplicateTransactionException.class,
                () -> service.recordSale(secondCart, "M1", "TXN-DUP", "second"),
                "recording a sale with a transaction id that already exists must be rejected with a DuplicateTransactionException before saving");

        Optional<TransactionRecord> stored = repo.findById("TXN-DUP");
        assertTrue(stored.isPresent());
        assertEquals(new BigDecimal("10.00"), stored.get().getAmount(), "the original record must be untouched, not overwritten by the rejected duplicate");
    }

    // ---------------------------------------------------------------
    // POS2-8: TransactionRecordRowMapper -- real Spring RowMapper<T> against a ResultSet.
    // The mapping-logic half needs no database (FakeResultSet); the delegation half proves
    // JdbcTransactionRepository actually routes through the mapper end to end.
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "POS2-8", points = 10, description = "TransactionRecordRowMapper correctly maps a ResultSet row, including a null/missing memo")
    public void transactionRecordRowMapper_mapsRowsIncludingMissingMemo() throws Exception {
        TransactionRecordRowMapper mapper = new TransactionRecordRowMapper();

        Map<String, Object> fullRow = new LinkedHashMap<>();
        fullRow.put("transaction_id", "TXN-RM1");
        fullRow.put("merchant_id", "M1");
        fullRow.put("amount", new BigDecimal("7.50"));
        fullRow.put("memo", "latte");
        fullRow.put("occurred_at", java.time.OffsetDateTime.now());
        TransactionRecord mapped = mapper.mapRow(FakeResultSet.of(fullRow), 1);
        assertEquals("TXN-RM1", mapped.getTransactionId());
        assertEquals("latte", mapped.getMemo());

        Map<String, Object> noMemoRow = new LinkedHashMap<>();
        noMemoRow.put("transaction_id", "TXN-RM2");
        noMemoRow.put("merchant_id", "M1");
        noMemoRow.put("amount", new BigDecimal("3.00"));
        noMemoRow.put("memo", null);
        noMemoRow.put("occurred_at", java.time.OffsetDateTime.now());
        TransactionRecord mappedNoMemo = assertDoesNotThrowAndReturn(() -> {
            try {
                return mapper.mapRow(FakeResultSet.of(noMemoRow), 1);
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        });
        assertEquals("", mappedNoMemo.getMemo(), "a null memo column must map to empty string, not throw");
    }

    @Test
    @GradedTest(tag = "POS2-8", points = 4, description = "JdbcTransactionRepository actually delegates to TransactionRecordRowMapper (not inline mapping)")
    public void repository_usesRowMapperForFindById() {
        assumeDbAvailable();
        JdbcTransactionRepository repo = new JdbcTransactionRepository(jdbcTemplate);
        repo.save(new TransactionRecord("TXN-RM3", "M1", new BigDecimal("2.00"), null, java.time.Instant.now()));
        Optional<TransactionRecord> found = repo.findById("TXN-RM3");
        assertTrue(found.isPresent());
        assertEquals("", found.get().getMemo(), "repository-mapped record must reflect RowMapper's null-memo handling");
    }

    // ---------------------------------------------------------------
    // POS2-9: Merchant equals()/hashCode() by identity -- unchanged, no persistence at all.
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "POS2-9", points = 6, description = "Merchant equals()/hashCode() by merchantId identity")
    public void merchant_equalsAndHashCodeByIdentity() {
        Merchant a = new Merchant("M1", "Corner Store");
        Merchant b = new Merchant("M1", "Renamed Corner Store");
        Merchant different = new Merchant("M2", "Corner Store");

        assertTrue(a.equals(b), "Merchants with the same merchantId must be equal even if displayName differs");
        assertEquals(a.hashCode(), b.hashCode(), "equal Merchants must have equal hashCodes");
        assertFalse(a.equals(different), "different merchantId must not be equal");

        Set<Merchant> set = new HashSet<>();
        set.add(a);
        set.add(b);
        assertEquals(1, set.size(), "a HashSet<Merchant> must collapse equal-by-identity instances to one entry");
    }

    private static <T> T assertDoesNotThrowAndReturn(java.util.function.Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            fail("Expected no exception but got " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return null;
        }
    }
}
