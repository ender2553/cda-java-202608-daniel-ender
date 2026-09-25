package com.cyberdev.secsuite.service;

import org.springframework.stereotype.Service;

import com.cyberdev.secsuite.exception.IngestionException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.repository.CveCatalogRepository;
import com.cyberdev.secsuite.repository.ThreatIntelAlertRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Automated ingestion of CSV threat intelligence feed files (SEC-10 parse + validate, SEC-11
 * persist + deduplicate).
 *
 * Expected file format -- UTF-8, one header row, then one alert per line, columns in EXACTLY
 * this order:
 * <pre>
 *   external_alert_id,source,indicator_type,indicator_value,related_cve_id,severity,description,published_at
 * </pre>
 *
 * Two levels of failure, deliberately different:
 *   FILE-level  (missing/unreadable file, not UTF-8, larger than MAX_FILE_BYTES, empty, wrong
 *               header)                    -> IngestionException, nothing is ingested.
 *   ROW-level   (any single field invalid) -> that row becomes a SkippedRow with a reason, and
 *               ingestion CONTINUES with the next row.
 *
 * DESIGN DECISION -- a related_cve_id that is not in cve_catalog is REJECTED (row skipped,
 * reason recorded), not ingested with a NULL foreign key. Referential integrity is enforced at
 * the APPLICATION layer, not left to the database: (1) the InMemory* repositories have no
 * foreign keys at all, so without this check the in-memory run would happily store a dangling
 * reference the Postgres run would reject -- two runs, two different stories; (2) on Postgres
 * the FK violation would surface mid-persist as a DataAccessException, aborting the batch half
 * way instead of cleanly skipping one row; (3) quietly NULLing the reference would destroy the
 * one piece of information that made a CVE-type alert meaningful (for indicator_type CVE the
 * schema's CHECK constraint forbids a NULL related_cve_id anyway). When the catalog is updated
 * to include the CVE, re-ingesting the same file picks the row up -- SEC-11's deduplication
 * makes that re-run safe.
 */
@Service
public class ThreatIntelCsvIngestionService {

    public static final String EXPECTED_HEADER =
            "external_alert_id,source,indicator_type,indicator_value,related_cve_id,severity,description,published_at";
    private static final int EXPECTED_COLUMNS = 8;

    /** Resource-exhaustion guard: a feed file bigger than this is refused outright. */
    public static final long MAX_FILE_BYTES = 5L * 1024 * 1024;

    private static final int MAX_ID_LENGTH = 100;
    private static final int MAX_SOURCE_LENGTH = 100;
    private static final int MAX_INDICATOR_LENGTH = 512;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private static final Pattern EXTERNAL_ID = Pattern.compile("^[A-Za-z0-9._:-]{1,100}$");
    private static final Pattern DOMAIN = Pattern.compile(
            "^(?=.{1,253}$)([A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$");
    private static final Pattern FILE_HASH = Pattern.compile("^([A-Fa-f0-9]{32}|[A-Fa-f0-9]{40}|[A-Fa-f0-9]{64})$");

    private final CveCatalogRepository cveCatalogRepository;
    private final ThreatIntelAlertRepository alertRepository;
    private final Clock clock;

    public ThreatIntelCsvIngestionService(CveCatalogRepository cveCatalogRepository,
                                          ThreatIntelAlertRepository alertRepository, Clock clock) {
        if (cveCatalogRepository == null || alertRepository == null || clock == null) {
            throw new ValidationException("ThreatIntelCsvIngestionService dependencies must not be null");
        }
        this.cveCatalogRepository = cveCatalogRepository;
        this.alertRepository = alertRepository;
        this.clock = clock;
    }

    /** GIVEN convenience: parse, then persist -- one complete ingestion run. */
    public IngestionSummary ingest(Path csvFile) {
        IngestionResult parsed = parseFile(csvFile);
        return new IngestionSummary(parsed, persistAndDeduplicate(parsed));
    }

    // INSTRUCTOR NOTE [SEC-10]: Concept tested: automated file ingestion that treats EVERY
    // byte of the file as untrusted input, and fails closed PER ROW without aborting the file.
    // Shape of a correct solution:
    //   File level (-> IngestionException, wrapping any IOException -- never let a raw
    //   IOException/NoSuchFileException escape): null path, file missing or not a regular file,
    //   file larger than MAX_FILE_BYTES (checked BEFORE reading it), bytes that are not valid
    //   UTF-8, empty file, or a header that is not exactly EXPECTED_HEADER (a UTF-8 BOM is
    //   tolerated). A wrong header means the columns cannot be trusted to mean what we think,
    //   so guessing would be failing open.
    //   Row level (-> SkippedRow, keep going): blank lines are ignored; every other line is
    //   split with CsvLineParser and handed to validateRow, which throws ValidationException on
    //   the first problem. The loop catches ValidationException ONLY, records
    //   SkippedRow(lineNumber, claimed id, reason) and continues. Physical line numbers are
    //   1-based with the header on line 1.
    //   Row rules (validateRow): exactly 8 fields; no control characters in any field;
    //   required non-blank: external_alert_id, source, indicator_type, indicator_value,
    //   severity, published_at; length caps; external_alert_id allow-listed characters;
    //   indicator_type parses to IndicatorType; severity parses to LOW/MEDIUM/HIGH/CRITICAL
    //   (NONE rejected); published_at parses as ISO-8601 instant, offset date-time, or plain
    //   date (taken as midnight UTC); indicator_value must match the shape of its type (IPv4,
    //   domain, 32/40/64 hex hash, CVE id); related_cve_id, when present, must be a
    //   well-formed CVE id that EXISTS in cve_catalog (design decision in the class javadoc);
    //   for indicator_type CVE, related_cve_id is required and must equal indicator_value.
    // Why catch ValidationException and not RuntimeException? A DataAccessException from the
    // catalog lookup means the DATABASE is down, not that the row is bad -- recording every
    // row as "skipped: invalid" would be a misleading report. That failure should propagate.
    // Common mistakes: (1) one bad row throws and the rest of the file is silently never read;
    // (2) String.split(",") -- breaks on the commas inside quoted descriptions; (3) trusting
    // the enum/severity text with valueOf() and letting IllegalArgumentException escape;
    // (4) LocalDate.parse only, so every timestamp with a time component is "malformed";
    // (5) Files.readAllLines on an unbounded file; (6) catching IOException and returning an
    // empty result -- a missing feed must be LOUD, not look like "no alerts today".
    // SECURITY CALLOUT: file content is untrusted input -- the same principle as a web form or
    // an API body. The feed is controlled by a third party (or by whoever can write to the
    // drop folder); allow-list every field, bound every size, and never let one hostile row
    // take down the whole run. Descriptions are stored as-is but are escaped again on OUTPUT
    // by the report (SEC-16), because validation on the way in does not make text safe for
    // every place it will later be rendered.
    public IngestionResult parseFile(Path csvFile) {
        throw new UnsupportedOperationException(
                "TODO [SEC-10]: file-level checks (IngestionException), then validate each row into parsed or skipped without aborting the file");
    }

    // INSTRUCTOR NOTE [SEC-11]: Concept tested: idempotent persistence with the SAME
    // check-before-write discipline as QuickPay's recordSale (POS2-7) and SEC-4. For each
    // parsed alert, in order: if alertRepository.findByExternalAlertId(id) is present, record
    // the id as a duplicate and move on -- do NOT throw (a feed re-sending an alert is normal,
    // not an error) and do NOT overwrite/upsert (the stored alert may already have been
    // triaged; a feed must not be able to silently rewrite history). Otherwise save it and
    // record the persisted alert returned by save(), not the transient id-0 input, as inserted.
    // Running the same file twice must leave the row count unchanged
    // the second time -- Main does exactly that and prints both runs. Because each check sees
    // the previous inserts, a file that contains the same id twice also inserts it once.
    // Common mistakes: (1) save() first and catch the unique-constraint DataAccessException as
    // "the duplicate check" -- works on Postgres by accident, is exception-driven control
    // flow, and on Postgres would abort a surrounding transaction; (2) deduplicating only
    // within the file (a Set of ids seen so far) but not against what is already stored;
    // (3) an "ON CONFLICT DO UPDATE" style upsert.
    // SECURITY CALLOUT: the check-then-insert has a race if two ingestions run concurrently;
    // the UNIQUE constraint on external_alert_id is the database-level backstop. Application
    // check for the clean, logged outcome; database constraint for the guarantee.
    public PersistenceResult persistAndDeduplicate(IngestionResult result) {
        throw new UnsupportedOperationException(
                "TODO [SEC-11]: save each parsed alert once; an already-stored external_alert_id is recorded as a duplicate, never thrown or overwritten");
    }

    /** Part of SEC-10: validates one split row and builds the alert, or throws ValidationException. */
    private ThreatIntelAlert validateRow(List<String> fields) {
        throw new UnsupportedOperationException(
                "TODO [SEC-10]: apply every row rule and build the alert, or throw ValidationException describing the first problem");
    }

    private static void validateIndicatorShape(IndicatorType type, String value) {
        boolean ok = switch (type) {
            case IP -> Asset.isValidIpv4(value);
            case DOMAIN -> DOMAIN.matcher(value).matches();
            case FILE_HASH -> FILE_HASH.matcher(value).matches();
            case CVE -> CveCatalogEntry.isWellFormedCveId(value);
        };
        if (!ok) {
            throw new ValidationException("indicator_value is not a valid " + type + " indicator");
        }
    }

    /** ISO-8601 instant ("2026-09-01T08:00:00Z"), offset date-time ("...+02:00"), or date ("2026-09-03"). */
    private static Instant parseTimestamp(String raw) {
        try {
            return Instant.parse(raw);
        } catch (DateTimeParseException ignored) {
            // try the next accepted format
        }
        try {
            return OffsetDateTime.parse(raw).toInstant();
        } catch (DateTimeParseException ignored) {
            // try the next accepted format
        }
        try {
            return LocalDate.parse(raw).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            throw new ValidationException("published_at '" + truncate(raw, 40) + "' is not an ISO-8601 date or timestamp");
        }
    }

    private static String required(String raw, String column, int maxLength) {
        String value = raw == null ? "" : raw.strip();
        if (value.isEmpty()) {
            throw new ValidationException("required field '" + column + "' is blank");
        }
        if (value.length() > maxLength) {
            throw new ValidationException("field '" + column + "' exceeds " + maxLength + " characters");
        }
        return value;
    }

    private static String optional(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.strip();
    }

    private static boolean containsControlCharacter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}
