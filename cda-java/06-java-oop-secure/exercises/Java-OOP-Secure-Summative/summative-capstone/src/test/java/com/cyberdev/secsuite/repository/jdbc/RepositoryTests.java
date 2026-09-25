package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.Component;
import com.cyberdev.secsuite.model.Criticality;
import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.model.StrideCategory;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.model.ThreatModelEntry;
import com.cyberdev.secsuite.model.ThreatModelEntryStatus;
import com.cyberdev.secsuite.repository.jdbc.mapper.AssetRowMapper;
import com.cyberdev.secsuite.repository.jdbc.mapper.ComponentRowMapper;
import com.cyberdev.secsuite.repository.jdbc.mapper.ThreatIntelAlertRowMapper;
import com.cyberdev.secsuite.repository.jdbc.mapper.ThreatModelEntryRowMapper;
import com.cyberdev.secsuite.support.FakeResultSet;
import com.cyberdev.secsuite.support.LiveDatabase;
import com.cyberdev.secsuite.testkit.GradedTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * The JDBC layer: SEC-3 (JdbcAssetRepository + AssetRowMapper), SEC-6 (components + the
 * component_cve link table), SEC-8 (JdbcThreatModelEntryRepository + mapper), SEC-9
 * (JdbcThreatIntelAlertRepository + mapper) and SEC-15 (the searchByHostname SQL injection
 * fix). Two kinds of test, exactly as in the QuickPay POS series:
 *
 *  - RowMapper tests use FakeResultSet (no database at all). They also catch the two classic
 *    mapper mistakes: calling rs.next() and reading columns by position.
 *  - Jdbc*Repository tests talk to a REAL PostgreSQL database (see support.LiveDatabase and
 *    REQUIREMENTS.md "Database setup"). If none is reachable they are SKIPPED, not failed.
 *    Each runs in a transaction that is rolled back afterwards, so the seeded data is never
 *    changed and the tests can be re-run any number of times.
 *
 * SEC-15 can ONLY be demonstrated against a real database: the in-memory AssetRepository has
 * no query language to inject into. Grading runs with the database available.
 */
public class RepositoryTests {

    private static final Long STOREFRONT_MODEL = 1L;
    private static final Long VPN_MODEL = 2L;
    private static final Instant NOW = Instant.parse("2026-09-24T09:00:00Z");

    private static LiveDatabase db;

    @BeforeAll
    static void connectToDatabase() {
        db = LiveDatabase.connect();
    }

    @AfterEach
    void rollbackTestTransaction() {
        db.rollback();
    }

    @AfterAll
    static void closeDatabase() {
        db.close();
    }

    private static String unique(String prefix) {
        return prefix + "-" + Long.toString(
                java.util.concurrent.ThreadLocalRandom.current().nextLong(10000000L, 99999999L));
    }

    // ---------------------------------------------------------------
    // SEC-3: AssetRowMapper (no DB) + JdbcAssetRepository (DB)
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-3", points = 1, description = "AssetRowMapper maps one row by column name, without calling rs.next()")
    public void assetRowMapper_mapsColumnsByName() throws SQLException {
        Long id = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("hostname", "web-prod-01");
        row.put("ip_address", "10.0.1.10");
        row.put("owner_team", "Platform Engineering");
        row.put("criticality", "CRITICAL");

        Asset asset = new AssetRowMapper().mapRow(FakeResultSet.of(row), 0);

        assertEquals(id, asset.getId());
        assertEquals("web-prod-01", asset.getHostname());
        assertEquals("10.0.1.10", asset.getIpAddress());
        assertEquals("Platform Engineering", asset.getOwnerTeam());
        assertEquals(Criticality.CRITICAL, asset.getCriticality());
    }

    @Test
    @GradedTest(tag = "SEC-3", points = 1, description = "AssetRowMapper fails closed on bad stored data (strict criticality parse, validating constructor)")
    public void assetRowMapper_failsClosedOnBadData() {
        AssetRowMapper mapper = new AssetRowMapper();
        Map<String, Object> badCriticality = assetRow("SUPER");
        assertThrows(ValidationException.class, () -> mapper.mapRow(FakeResultSet.of(badCriticality), 0),
                "an unknown criticality must be rejected, not guessed");
        Map<String, Object> nullCriticality = assetRow(null);
        assertThrows(ValidationException.class, () -> mapper.mapRow(FakeResultSet.of(nullCriticality), 0),
                "a NULL criticality must be rejected, not defaulted");
        Map<String, Object> badIp = assetRow("LOW");
        badIp.put("ip_address", "999.1.1.1");
        assertThrows(ValidationException.class, () -> mapper.mapRow(FakeResultSet.of(badIp), 0),
                "the mapper must build the Asset through its validating constructor");
    }

    @Test
    @GradedTest(tag = "SEC-3", points = 1, description = "[DB] JdbcAssetRepository save/findById/findByHostname round trip; unknown keys give Optional.empty()")
    public void jdbcAsset_saveAndFindRoundTrip() {
        db.assumeAvailable();
        JdbcAssetRepository repository = new JdbcAssetRepository(db.jdbcTemplate());
        Asset probe = new Asset(0L, unique("sec3-probe"), "10.9.9.9", "Test Team", Criticality.MEDIUM);

        Asset saved = repository.save(probe);

        assertEquals(0L, probe.getId(), "save must not mutate the transient input");
        assertTrue(saved.getId() > 0, "save returns a copy with the generated identity");
        Asset byId = repository.findById(saved.getId()).orElseThrow(() -> new AssertionError("saved asset not found by id"));
        assertEquals(probe.getHostname(), byId.getHostname());
        assertEquals("10.9.9.9", byId.getIpAddress());
        assertEquals("Test Team", byId.getOwnerTeam());
        assertEquals(Criticality.MEDIUM, byId.getCriticality(), "criticality is stored by name() and parsed back");
        assertTrue(repository.findByHostname(probe.getHostname()).isPresent());
        assertTrue(repository.findById(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE)).isEmpty(), "unknown id -> Optional.empty(), not an exception");
        assertTrue(repository.findByHostname("no-such-host.invalid").isEmpty());
    }

    @Test
    @GradedTest(tag = "SEC-3", points = 1, description = "[DB] findAll is ORDER BY hostname; a quote in a lookup value is just data")
    public void jdbcAsset_findAllOrderedAndQuotesAreData() {
        db.assumeAvailable();
        JdbcAssetRepository repository = new JdbcAssetRepository(db.jdbcTemplate());
        repository.save(new Asset(0L, "zz-sec3-last", "10.9.9.10", "Test Team", Criticality.LOW));
        repository.save(new Asset(0L, "aa-sec3-first", "10.9.9.11", "Test Team", Criticality.LOW));

        List<String> hostnames = repository.findAll().stream().map(Asset::getHostname).toList();

        assertTrue(hostnames.containsAll(List.of("web-prod-01", "db-prod-01", "aa-sec3-first", "zz-sec3-last")));
        assertTrue(hostnames.indexOf("aa-sec3-first") < hostnames.indexOf("web-prod-01")
                        && hostnames.indexOf("web-prod-01") < hostnames.indexOf("zz-sec3-last"),
                "findAll must be ORDER BY hostname (SQL guarantees no order without it)");
        Optional<Asset> quoted = assertDoesNotThrow(() -> repository.findByHostname("o'brien"),
                "a quote in the lookup value must be bound as data, never break the SQL");
        assertTrue(quoted.isEmpty());
    }

    // ---------------------------------------------------------------
    // SEC-6: ComponentRowMapper (no DB) + JdbcComponentRepository (DB)
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-6", points = 1, description = "ComponentRowMapper maps one component row by column name")
    public void componentRowMapper_mapsColumnsByName() throws SQLException {
        Long id = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("application_name", "storefront-web");
        row.put("component_name", "org.fictional:loglite");
        row.put("component_version", "2.14.0");
        row.put("ecosystem", "Maven");

        Component component = new ComponentRowMapper().mapRow(FakeResultSet.of(row), 3);

        assertEquals(id, component.getId());
        assertEquals("storefront-web", component.getApplicationName());
        assertEquals("org.fictional:loglite", component.getComponentName());
        assertEquals("2.14.0", component.getComponentVersion());
        assertEquals("Maven", component.getEcosystem());
    }

    @Test
    @GradedTest(tag = "SEC-6", points = 2, description = "[DB] components save/find/findAll ordering; link table insert; CVE ids per component (empty list when none)")
    public void jdbcComponent_saveLinkAndFind() {
        db.assumeAvailable();
        JdbcComponentRepository repository = new JdbcComponentRepository(db.jdbcTemplate());
        String app = unique("sec6-app");
        Component newer = repository.save(new Component(0L, app, "sec6-lib", "2.0.0", "Maven"));
        Component older = repository.save(new Component(0L, app, "sec6-lib", "1.0.0", "Maven"));
        repository.linkCve(newer.getId(), "CVE-2023-91004");
        repository.linkCve(newer.getId(), "CVE-2023-91001");

        assertEquals(List.of("CVE-2023-91001", "CVE-2023-91004"), repository.findCveIdsByComponentId(newer.getId()),
                "linked CVE ids, ORDER BY cve_id");
        List<String> none = repository.findCveIdsByComponentId(older.getId());
        assertNotNull(none, "a component with no CVEs returns an EMPTY list, never null");
        assertTrue(none.isEmpty());
        assertEquals("sec6-lib", repository.findById(older.getId()).orElseThrow().getComponentName());
        assertTrue(repository.findById(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE)).isEmpty());

        List<Long> ids = repository.findAll().stream().map(Component::getId).toList();
        assertTrue(ids.contains(older.getId()) && ids.contains(newer.getId()),
                "findAll lists every component, linked or not (no INNER JOIN to component_cve)");
        assertEquals(ids.size(), ids.stream().distinct().count(), "no component may appear twice");
        assertTrue(ids.indexOf(older.getId()) < ids.indexOf(newer.getId()),
                "findAll is ORDER BY application_name, component_name, component_version");
        assertEquals(List.of("CVE-2023-91001"),
                repository.findCveIdsByComponentId(1L),
                "the seeded loglite component is linked to CVE-2023-91001");
    }

    // ---------------------------------------------------------------
    // SEC-8 (JDBC half): ThreatModelEntryRowMapper (no DB) + repository (DB)
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-8", points = 1, description = "ThreatModelEntryRowMapper: strict enum parsing, NULL mitigation stays null")
    public void threatModelEntryRowMapper_enumsAndNullMitigation() throws SQLException {
        ThreatModelEntryRowMapper mapper = new ThreatModelEntryRowMapper();
        Long id = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("threat_model_id", STOREFRONT_MODEL);
        row.put("stride_category", "INFORMATION_DISCLOSURE");
        row.put("description", "Session tokens exposed in URL query strings");
        row.put("mitigation", null);
        row.put("status", "IDENTIFIED");

        ThreatModelEntry entry = mapper.mapRow(FakeResultSet.of(row), 0);

        assertEquals(id, entry.getId());
        assertEquals(STOREFRONT_MODEL, entry.getThreatModelId());
        assertEquals(StrideCategory.INFORMATION_DISCLOSURE, entry.getStrideCategory());
        assertNull(entry.getMitigation(), "a NULL mitigation stays null");
        assertEquals(ThreatModelEntryStatus.IDENTIFIED, entry.getStatus());

        row.put("stride_category", "PHISHING");
        assertThrows(ValidationException.class, () -> mapper.mapRow(FakeResultSet.of(row), 0),
                "a category outside the STRIDE enum must fail closed, never be re-filed under a guess");
    }

    @Test
    @GradedTest(tag = "SEC-8", points = 1, description = "[DB] threat model entries round-trip (enums by name, NULL mitigation); unknown model gives an empty list")
    public void jdbcThreatModelEntry_saveAndFind() {
        db.assumeAvailable();
        JdbcThreatModelEntryRepository repository = new JdbcThreatModelEntryRepository(db.jdbcTemplate());
        ThreatModelEntry transientEntry = new ThreatModelEntry(0L, STOREFRONT_MODEL, StrideCategory.REPUDIATION,
                unique("Customer disputes an order and there is no signed audit record"), null,
                ThreatModelEntryStatus.IDENTIFIED);

        ThreatModelEntry entry = repository.save(transientEntry);

        ThreatModelEntry stored = repository.findByThreatModelId(STOREFRONT_MODEL).stream()
                .filter(e -> e.getId().equals(entry.getId())).findFirst()
                .orElseThrow(() -> new AssertionError("saved entry not returned for its model"));
        assertEquals(StrideCategory.REPUDIATION, stored.getStrideCategory());
        assertNull(stored.getMitigation(), "NULL mitigation must round-trip as null");
        assertEquals(ThreatModelEntryStatus.IDENTIFIED, stored.getStatus());
        assertTrue(repository.findByThreatModelId(VPN_MODEL).stream().noneMatch(e -> e.getId().equals(entry.getId())),
                "entries of one model must not leak into another");
        List<ThreatModelEntry> none = repository.findByThreatModelId(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
        assertNotNull(none);
        assertTrue(none.isEmpty(), "an unknown model has no entries -- the repository must not invent any");
    }

    // ---------------------------------------------------------------
    // SEC-9: ThreatIntelAlertRowMapper (no DB) + repository (DB)
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-9", points = 1, description = "ThreatIntelAlertRowMapper handles every nullable column (no NPE on a NULL published_at)")
    public void alertRowMapper_nullableColumns() throws SQLException {
        ThreatIntelAlertRowMapper mapper = new ThreatIntelAlertRowMapper();
        Long id = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Map<String, Object> row = alertRow(id, "IP", "HIGH");
        row.put("related_cve_id", null);
        row.put("description", null);
        row.put("published_at", null);

        ThreatIntelAlert sparse = mapper.mapRow(FakeResultSet.of(row), 0);

        assertEquals(id, sparse.getId());
        assertEquals("TI-MAP-1", sparse.getExternalAlertId());
        assertEquals(IndicatorType.IP, sparse.getIndicatorType());
        assertEquals(Severity.HIGH, sparse.getSeverity());
        assertNull(sparse.getRelatedCveId());
        assertNull(sparse.getDescription());
        assertNull(sparse.getPublishedAt(), "a NULL published_at must map to null, not throw");
        assertEquals(NOW, sparse.getIngestedAt());

        Map<String, Object> full = alertRow(id, "CVE", "CRITICAL");
        full.put("indicator_value", "CVE-2023-91001");
        full.put("related_cve_id", "CVE-2023-91001");
        full.put("description", "exploited");
        full.put("published_at", Timestamp.from(Instant.parse("2026-09-01T08:00:00Z")));
        ThreatIntelAlert rich = mapper.mapRow(FakeResultSet.of(full), 1);
        assertEquals("CVE-2023-91001", rich.getRelatedCveId());
        assertEquals(Instant.parse("2026-09-01T08:00:00Z"), rich.getPublishedAt());
    }

    @Test
    @GradedTest(tag = "SEC-9", points = 1, description = "ThreatIntelAlertRowMapper fails closed on a stored severity NONE or an unknown indicator type")
    public void alertRowMapper_failsClosedOnBadData() {
        ThreatIntelAlertRowMapper mapper = new ThreatIntelAlertRowMapper();
        Map<String, Object> noneSeverity = alertRow(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE), "IP", "NONE");
        assertThrows(ValidationException.class, () -> mapper.mapRow(FakeResultSet.of(noneSeverity), 0),
                "NONE is not a storable alert severity");
        Map<String, Object> badType = alertRow(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE), "URL", "LOW");
        assertThrows(ValidationException.class, () -> mapper.mapRow(FakeResultSet.of(badType), 0),
                "URL is not an IndicatorType");
    }

    @Test
    @GradedTest(tag = "SEC-9", points = 1, description = "[DB] save binds NULLs safely; findByExternalAlertId is an EXACT match; count() increments")
    public void jdbcAlert_saveWithNullsAndExactLookup() {
        db.assumeAvailable();
        JdbcThreatIntelAlertRepository repository = new JdbcThreatIntelAlertRepository(db.jdbcTemplate());
        long before = repository.count();
        String externalId = unique("SEC9-PROBE");
        ThreatIntelAlert alert = new ThreatIntelAlert(0L, externalId, "O'Reilly Threat Feed",
                IndicatorType.DOMAIN, "bad.example.org", null, Severity.MEDIUM, null, null, NOW);

        alert = repository.save(alert);

        ThreatIntelAlert stored = repository.findByExternalAlertId(externalId)
                .orElseThrow(() -> new AssertionError("saved alert not found by external id"));
        assertEquals("O'Reilly Threat Feed", stored.getSource(), "feed text with a quote is stored exactly");
        assertNull(stored.getRelatedCveId());
        assertNull(stored.getDescription());
        assertNull(stored.getPublishedAt(), "a NULL published_at must be bound as NULL (guard Timestamp.from)");
        assertEquals(NOW, stored.getIngestedAt());
        assertEquals(before + 1, repository.count());
        assertTrue(repository.findByExternalAlertId("SEC9-PROBE%").isEmpty(),
                "the dedup lookup must be an exact '= ?' match, never LIKE");
        assertTrue(repository.findByExternalAlertId("no-such-alert").isEmpty());
    }

    @Test
    @GradedTest(tag = "SEC-9", points = 1, description = "[DB] findAll and findByIndicatorType bind the type and return only that type")
    public void jdbcAlert_findByIndicatorType() {
        db.assumeAvailable();
        JdbcThreatIntelAlertRepository repository = new JdbcThreatIntelAlertRepository(db.jdbcTemplate());
        String externalId = unique("SEC9-CVE");
        repository.save(new ThreatIntelAlert(0L, externalId, "Feed", IndicatorType.CVE, "CVE-2023-91004",
                "CVE-2023-91004", Severity.HIGH, "gadget chain", Instant.parse("2026-09-05T09:30:00Z"), NOW));

        List<ThreatIntelAlert> cveAlerts = repository.findByIndicatorType(IndicatorType.CVE);

        assertTrue(cveAlerts.stream().allMatch(a -> a.getIndicatorType() == IndicatorType.CVE),
                "only CVE-type alerts may be returned");
        List<String> ids = cveAlerts.stream().map(ThreatIntelAlert::getExternalAlertId).toList();
        assertTrue(ids.contains("TI-SEED-001") && ids.contains(externalId));
        assertFalse(ids.contains("TI-SEED-003"), "TI-SEED-003 is an IP indicator");
        assertEquals(repository.count(), repository.findAll().size(), "findAll returns every alert");
    }

    // ---------------------------------------------------------------
    // SEC-15: searchByHostname -- SQL injection fixed with a bound parameter
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-15", points = 1, description = "[DB] an honest keyword still finds every matching host (the fix must not break the feature)")
    public void search_honestKeywordStillWorks() {
        db.assumeAvailable();
        JdbcAssetRepository repository = new JdbcAssetRepository(db.jdbcTemplate());

        List<String> hostnames = repository.searchByHostname("prod").stream().map(Asset::getHostname).toList();

        assertTrue(hostnames.containsAll(List.of("db-prod-01", "web-prod-01")), "substring search for 'prod'");
        assertTrue(hostnames.stream().allMatch(h -> h.contains("prod")), "only hosts containing 'prod'");
        assertTrue(repository.searchByHostname(null).isEmpty(), "null keyword -> empty list");
    }

    @Test
    @GradedTest(tag = "SEC-15", points = 2, description = "[DB] tautology payloads (' OR 1=1 --) return NO rows instead of the whole table")
    public void search_tautologyPayloadsReturnNothing() {
        db.assumeAvailable();
        JdbcAssetRepository repository = new JdbcAssetRepository(db.jdbcTemplate());

        for (String payload : List.of("x' OR 1=1 --", "' OR '1'='1")) {
            List<Asset> result = assertDoesNotThrow(() -> repository.searchByHostname(payload),
                    "the payload must be treated as literal text: " + payload);
            assertTrue(result.isEmpty(), "no hostname contains the text " + payload
                    + " -- returning rows means the keyword became part of the SQL (use a bound ? parameter)");
        }
    }

    @Test
    @GradedTest(tag = "SEC-15", points = 2, description = "[DB] a lone quote and a UNION exfiltration payload are literal text: no SQL error, no leaked analyst rows")
    public void search_quoteAndUnionPayloadsAreLiteralText() {
        db.assumeAvailable();
        // An analyst row the UNION payload would try to exfiltrate (rolled back after the test).
        new JdbcAnalystRepository(db.jdbcTemplate()).save(new Analyst(0L, "sec15-probe",
                "pbkdf2_sha256$120000$c2FsdA==$aGFzaA==", "ciphertext", NOW));
        JdbcAssetRepository repository = new JdbcAssetRepository(db.jdbcTemplate());

        String union = "x' UNION SELECT id, username, '10.0.0.1', password_hash, 'LOW' FROM analyst --";
        List<Asset> leaked = assertDoesNotThrow(() -> repository.searchByHostname(union),
                "the UNION payload must be treated as literal text");
        assertTrue(leaked.isEmpty(), "the asset search leaked " + leaked.size()
                + " analyst row(s) shaped as assets -- a credential-hash exfiltration endpoint");

        List<Asset> quoted = assertDoesNotThrow(() -> repository.searchByHostname("o'brien"),
                "a single quote in the keyword must not produce a SQL syntax error");
        assertTrue(quoted.isEmpty());
    }

    // ---------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------

    private static Map<String, Object> assetRow(String criticality) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
        row.put("hostname", "web-prod-01");
        row.put("ip_address", "10.0.1.10");
        row.put("owner_team", "Platform Engineering");
        row.put("criticality", criticality);
        return row;
    }

    private static Map<String, Object> alertRow(Long id, String indicatorType, String severity) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("external_alert_id", "TI-MAP-1");
        row.put("source", "NorthStar ISAC");
        row.put("indicator_type", indicatorType);
        row.put("indicator_value", "203.0.113.45");
        row.put("related_cve_id", null);
        row.put("severity", severity);
        row.put("description", "d");
        row.put("published_at", null);
        row.put("ingested_at", Timestamp.from(NOW));
        return row;
    }
}
