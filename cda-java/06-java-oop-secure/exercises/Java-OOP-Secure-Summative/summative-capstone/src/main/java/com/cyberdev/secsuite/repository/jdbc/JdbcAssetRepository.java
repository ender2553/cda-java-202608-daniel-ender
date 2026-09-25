package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.repository.AssetRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.AssetRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

/**
 * AssetRepository backed by the PostgreSQL "asset" table via Spring's JdbcTemplate (SEC-3,
 * SEC-15).
 *
 * INSTRUCTOR NOTE -- why @Repository, and why nothing else from Spring: @Repository
 * (org.springframework.stereotype, from spring-context) is Spring's standard stereotype for
 * "this class is a persistence-layer component". It is a legitimate, self-documenting marker
 * even with NO Spring container running, which is the case in this project: Main builds this
 * class with `new JdbcAssetRepository(jdbcTemplate)` -- plain constructor injection, the same
 * manual DI as every prior project. What the annotation does NOT do here: it does not register
 * a bean (nothing scans for it), and Spring's @Repository exception translation (the
 * PersistenceExceptionTranslationPostProcessor) is not active because no container exists to
 * run it. That is fine: JdbcTemplate ITSELF already translates every java.sql.SQLException
 * into Spring's org.springframework.dao.DataAccessException hierarchy, and each method below
 * then wraps that in this application's own exception.DataAccessException. Deliberately NOT
 * used: @Autowired (on a field or constructor) -- with no container, nothing would ever
 * satisfy it; putting it here would be cargo-cult Spring, an annotation with no machinery
 * behind it. If this app were later moved into a Spring container, the single public
 * constructor would already be picked for constructor injection with no annotation needed.
 */
@Repository
@Profile("jdbc")
public class JdbcAssetRepository implements AssetRepository {

    private static final String SELECT_COLUMNS =
            "SELECT id, hostname, ip_address, owner_team, criticality FROM asset";

    private final JdbcTemplate jdbcTemplate;
    private final AssetRowMapper rowMapper = new AssetRowMapper();

    public JdbcAssetRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    // INSTRUCTOR NOTE [SEC-3]: Concept tested: parameterized CRUD through JdbcTemplate, every
    // value bound through a "?" placeholder -- never concatenated into the SQL text -- and
    // every org.springframework.dao.DataAccessException re-wrapped in
    // com.cyberdev.secsuite.exception.DataAccessException (so services never see a Spring
    // type). save(): INSERT the non-id columns with bound parameters, RETURNING id, then return
    // asset.withId(id); never mutate the transient entity. The enum is stored by name(), matching the CHECK
    // constraint. findById / findByHostname: jdbcTemplate.query(sql, rowMapper, arg), then
    // "empty list -> Optional.empty(), else Optional.of(first)" (the POS convention; avoid
    // queryForObject, which THROWS EmptyResultDataAccessException for "not found" and turns a
    // normal outcome into an exception). findAll: ORDER BY hostname, because SQL guarantees NO
    // row order without ORDER BY and the report must be reproducible. Common mistakes:
    // concatenating even "safe-looking" values like the Long ("it's just a Long" is how
    // injection habits start); criticality.toString() or ordinal() instead of name(); catching
    // Spring's exception and returning Optional.empty() -- a database outage must not look
    // like "no such asset"; writing `catch (DataAccessException e)` with the WRONG import,
    // catching this app's type instead of Spring's (hence Spring's is fully qualified below).
    @Override
    public Asset save(Asset asset) {
        if (asset == null) {
            throw new ValidationException("asset must not be null");
        }
        try {
            Long id = jdbcTemplate.queryForObject(
                    "INSERT INTO asset (hostname, ip_address, owner_team, criticality) "
                            + "VALUES (?, ?, ?, ?) RETURNING id",
                    Long.class,
                    asset.getHostname(),
                    asset.getIpAddress(),
                    asset.getOwnerTeam(),
                    asset.getCriticality().name());

            return asset.withId(id);

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save asset " + asset.getHostname(), e);
        }
    }

    @Override
    public Optional<Asset> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            List<Asset> results = jdbcTemplate.query(
                    SELECT_COLUMNS + " WHERE id = ?",
                    rowMapper,
                    id
            );

            if (results.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(results.get(0));

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException(
                    "Failed to find asset by id " + id, e);
        }
    }

    @Override
    public Optional<Asset> findByHostname(String hostname) {
        if (hostname == null) {
            return Optional.empty();
        }

        try {
            List<Asset> results = jdbcTemplate.query(
                    SELECT_COLUMNS + " WHERE hostname = ?",
                    rowMapper,
                    hostname
            );

            if (results.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(results.get(0));

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException(
                    "Failed to find asset by hostname " + hostname, e);
        }
    }

    @Override
    public List<Asset> findAll() {
        try {
            return jdbcTemplate.query(
                    SELECT_COLUMNS + " ORDER BY hostname",
                    rowMapper
            );

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException(
                    "Failed to find all assets", e);
        }
    }

    // INSTRUCTOR NOTE [SEC-15]: THIS IS THE VULNERABLE STARTER, shipped AS-IS on purpose ("break
    // it, then fix it" -- same convention as QuickPay POS2-3 / library LIB-4/5). It compiles and
    // "works" for an honest keyword like "prod"; the graded TODO is the fix.
    // TODO [SEC-15]: replace the body of searchByHostname below with a parameterized query (constant
    // SQL text, the keyword bound through "?"). There is deliberately NO UnsupportedOperationException
    // here -- the method runs, it is just exploitable. Run the SEC-15 tests before AND after your fix.
    //
    //   EXPLOITS against the vulnerable version (verified against the seeded PostgreSQL 16
    //   database in the instructor build, connected as secsuite_app):
    //   1. keyword = x' OR 1=1 --
    //        ... WHERE hostname LIKE '%x' OR 1=1 --%' ORDER BY hostname
    //      The quote closes the string literal, OR 1=1 makes the WHERE clause always true, and
    //      "--" comments out the rest. Result: every asset, although no hostname contains "x".
    //   2. keyword = x' UNION SELECT id, username, '10.0.0.1', password_hash, 'LOW' FROM analyst --
    //      Result: analyst rows come back SHAPED AS ASSETS -- each username appears as a
    //      "hostname" and each PBKDF2 password hash as an "owner team". An asset search box
    //      just became a credential-hash exfiltration endpoint. (secsuite_app legitimately has
    //      SELECT on analyst -- AuthService needs it -- so least privilege does NOT stop this
    //      one. Only the parameterized query does.)
    //   3. keyword = x'; DELETE FROM asset; --
    //      A stacked second statement. Whether a driver will run it depends on how it executes
    //      multi-statement strings; what is certain is that it fails with "permission denied
    //      for table asset" when connected as secsuite_app, because dcl.sql grants no DELETE.
    //      Least privilege bounds the blast radius; it does not remove the vulnerability.
    //
    //   THE FIX: keep the SQL text constant and send the keyword as a bound parameter
    //   (JdbcTemplate.query(String sql, RowMapper<T> rowMapper, Object... args)). The "%...%"
    //   wildcards are added to the PARAMETER VALUE, not to the SQL string. Now every payload
    //   above is just an odd substring that no hostname contains -> zero rows.
    //   Polish (not required for full credit): LIKE treats % and _ inside the keyword as
    //   wildcards, which is not injection but is surprising ("web_prod" would match
    //   "web-prod"); the reference solution escapes them and declares ESCAPE '\' so the JDBC
    //   and in-memory implementations agree on plain substring semantics.
    //   Common near-misses (partial credit at most): blacklisting quotes, "--" or the literal
    //   substring "OR 1=1" before concatenating (bypassed by the UNION payload, by case
    //   changes, by /* */ comments...); "escaping" quotes by doubling them by hand; validating
    //   the keyword against the hostname regex and still concatenating (fragile: the
    //   protection now depends on a regex in a different class never being loosened).
    // SECURITY CALLOUT: parameterization is not an escaping technique -- the value never
    // becomes part of the SQL text at all, so there is nothing to escape and nothing to get
    // wrong. Validation, blacklists and least privilege are defense in depth AROUND it, never
    // a substitute FOR it.
    @Override
    public List<Asset> searchByHostname(String keyword) {
        if (keyword == null) {
            return List.of();
        }

        try {
            String sql = SELECT_COLUMNS
                    + " WHERE hostname LIKE ? ORDER BY hostname";

            return jdbcTemplate.query(
                    sql, rowMapper,
                    "%" + keyword + "%"
            );
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException(
                    "Failed to search assets by hostname", e);
        }
    }
}