package org.example.legacy;

import org.example.model.Dependency;
import org.example.util.InputValidator;
import org.example.util.SecurityLogger;
import org.example.util.ValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * STUDENT TODO VERSION of a legacy pipe-delimited dependency-scan log
 * reader: "name|version|ecosystem|cvssScore|cve1;cve2".
 *
 * This class originally shipped with five bugs, all present in one
 * method. Your job is to write a correct implementation from scratch --
 * use the bug descriptions below as a checklist of mistakes NOT to
 * reproduce:
 *
 *   BUG 1 (deny-list): checked `if (ecosystem.equals("malicious-repo"))
 *          continue;` instead of validating against an allow-list of
 *          known-good ecosystems. Anything NOT named "malicious-repo" was
 *          accepted, including typos, empty strings, and made-up values.
 *   BUG 2 (swallowed exception): wrapped the whole per-line parse in
 *          `catch (Exception e) { }` -- silently dropping the line with
 *          no record that anything went wrong, hiding both bugs in the
 *          input data and bugs in the parser itself.
 *   BUG 3 (resource leak): opened `new BufferedReader(new FileReader(path))`
 *          without try-with-resources or a finally block, so an exception
 *          thrown while reading a line left the file handle open.
 *   BUG 4 (verbose exception to user-facing output): called
 *          `e.printStackTrace()` (to System.out), printing the full
 *          internal stack trace -- including file paths -- to whatever
 *          console or log the analyst (or, worse, an end user) is
 *          watching.
 *   BUG 5 (coercion instead of fail-closed): on a NumberFormatException
 *          while parsing the CVSS score, defaulted the score to 0.0 and
 *          kept the record instead of rejecting it -- silently turning a
 *          malformed, possibly-critical vulnerability record into one
 *          that looks harmless.
 *
 * REQUIRED FIX: try-with-resources, catch specific exceptions, allow-list
 * validation via InputValidator, fail-closed rejection of malformed
 * lines, and safe error surfaces via SecurityLogger.
 */
public final class LegacyDependencyScanner {

    public static final class ScanReadResult {
        public final List<Dependency> accepted = new ArrayList<>();
        public int rejectedCount = 0;
        public final List<String> rejectionReasons = new ArrayList<>();
    }

    /**
     * TODO: read a legacy pipe-delimited dependency scan log. Malformed
     * lines must be rejected (fail-closed) and counted; a malformed line
     * must never crash the whole scan and must never get "fixed up" with
     * a default value.
     *
     *   - Open the file with try-with-resources: Files.newBufferedReader(
     *     path, StandardCharsets.UTF_8) -- this guarantees the reader is
     *     closed whether the loop completes normally or a line throws
     *     (fixes BUG 3).
     *   - Track a lineNumber counter starting at 0, incremented at the
     *     top of the loop before processing each line.
     *   - Skip blank lines (line.isBlank()) without counting them as
     *     rejections.
     *   - For each non-blank line, call parseLine(line) inside a
     *     try/catch for (ValidationException | NumberFormatException e):
     *     on success, add the returned Dependency to result.accepted; on
     *     failure, increment result.rejectedCount, build a reason string
     *     "line " + lineNumber + ": " + e.getMessage() and add it to
     *     result.rejectionReasons, then call
     *     SecurityLogger.logDetailed("LegacyDependencyScanner: rejected
     *     malformed scan line", e) -- full detail goes to the server-side
     *     log, never to stdout/stderr (fixes BUG 2 and BUG 4).
     *   - Do NOT use a bare `catch (Exception e) { }` anywhere in this
     *     method (that was BUG 2).
     */
    public ScanReadResult readLegacyScanLog(Path path) throws IOException {
        throw new UnsupportedOperationException("TODO: implement readLegacyScanLog()");
    }

    /**
     * TODO: parse one pipe-delimited line into a validated Dependency.
     *   - Split on "\\|" with limit -1 (so trailing empty fields, e.g. an
     *     empty CVE list, are preserved rather than dropped). Require
     *     exactly 5 fields; throw ValidationException naming the actual
     *     count otherwise.
     *   - parts[0] -> InputValidator.validateTextField(..., "name", 200).
     *   - parts[1] -> InputValidator.validateTextField(..., "version", 100).
     *   - parts[2] -> InputValidator.validateEcosystem(...) (fixes BUG 1
     *     -- this is an ALLOW-list check, not a check against one known
     *     bad value).
     *   - parts[3]: parse with Double.parseDouble(parts[3].trim()) inside
     *     a try/catch for NumberFormatException; on failure, re-throw as
     *     a ValidationException with a descriptive message (fixes BUG 5
     *     -- do NOT catch-and-default to 0.0). On success, validate the
     *     parsed value with InputValidator.validateCvssScore(...).
     *   - parts[4]: if blank, use an empty CVE list. Otherwise split on
     *     ";", skip blank entries, and validate each with
     *     InputValidator.validateCveId(rawCve.trim()), collecting into a
     *     new ArrayList<String>.
     *   - Construct and return new Dependency(name, version, ecosystem, cves).
     */
    private Dependency parseLine(String line) throws ValidationException {
        throw new UnsupportedOperationException("TODO: implement parseLine()");
    }
}
