package org.example;
/*

 *
 * This afternoon you're applying everything from this morning —
 * debugging,file I/O and Unit Testing — to the same
 * security-tool narrative you've been building all week.
 *
 * You'll need your Day 5 Vulnerability.java and Asset.java (reference
 * copies are included in this lab folder if yours differ).
 *
 * ------------------------------------------------------------
 * TODO 1 — Fields
 * ------------------------------------------------------------
 * Declare two fields:
 *   - a List<Vulnerability> to hold every finding in the register
 *   - a Set<String> to track every unique CVE ID currently in the
 *     register (used to detect duplicates before they're added)
 * Initialize both in the constructor.
 *
 * ------------------------------------------------------------
 * TODO 2 — validateCveId(String candidate)
 * ------------------------------------------------------------
 * A CVE ID must look like CVE-YYYY-NNNN (a 4-digit year, then a
 * sequence number of 4 to 7 digits — e.g. CVE-2024-1234 or
 * CVE-2024-1234567). Using the edge-case catalog from this morning,
 * this method should throw IllegalArgumentException for:
 *   - null
 *   - blank / empty string
 *   - anything that doesn't match the CVE-YYYY-NNNN shape (this
 *     single regex check naturally covers "malformed" AND "overflow" —
 *     a sequence number of 8+ digits will not match)
 * A well-formed CVE ID should simply return with no exception.
 * (Hint: java.util.regex.Pattern / String.matches())
 *
 * ------------------------------------------------------------
 * TODO 3 — validateComponent(String candidate)
 * ------------------------------------------------------------
 * A component name (e.g. "openssl", "log4j-core") must be:
 *   - not null, not blank
 *   - no more than 50 characters
 *   - free of '|' and line breaks — saveToFile() below writes each
 *     finding as one pipe-delimited line, so a component name
 *     containing '|' or a newline could inject a fake field the next
 *     time the register is loaded (the same injection-shaped-input
 *     concern as this morning's Adventurer name field, aimed at a
 *     different field in a different file format)
 * Throw IllegalArgumentException for any violation.
 *
 * ------------------------------------------------------------
 * TODO 4 — addFinding(Vulnerability finding)
 * ------------------------------------------------------------
 * Before adding anything:
 *   - call validateCveId(finding.getCveId())
 *   - call validateComponent(finding.getComponent())
 *   - if finding.getCveId() is already in your Set of known CVE IDs,
 *     throw DuplicateFindingException instead of adding it
 * Only if all three checks pass: add the finding to your List, and
 * add its CVE ID to your Set.
 *
 * ------------------------------------------------------------
 * TODO 5 — Encapsulated getters
 * ------------------------------------------------------------
 * getFindings() and getUniqueCveIds() should return views that a
 * caller cannot use to mutate this object's real internal state
 * (Collections.unmodifiableList / unmodifiableSet).
 *
 * ------------------------------------------------------------
 * TODO 6 — saveToFile(String filename) throws IOException
 * ------------------------------------------------------------
 * Write one line per finding, pipe-delimited, in this exact order:
 *   cveId|component|version|severity|estimatedRemediationCost|discoveredDate
 * Example line:
 *   CVE-2024-1234|openssl|3.0.1|CRITICAL|150.00|2024-01-15
 * Use try-with-resources so the writer is closed even if something
 * goes wrong partway through.
 *
 * ------------------------------------------------------------
 * TODO 7 — loadFromFile(String filename)
 *           throws IOException, InvalidRegisterDataException
 * ------------------------------------------------------------
 * Read the file saveToFile() produces, one line at a time. For each
 * line: split on "|" and require EXACTLY 6 parts — reject the line
 * with InvalidRegisterDataException otherwise. Parse each part
 * (Severity.valueOf, new BigDecimal(String), LocalDate.parse), and
 * wrap any parsing failure in InvalidRegisterDataException rather
 * than letting the raw exception escape. Only add a finding to this
 * register once its full line has been validated — this method
 * should build and return a brand-new RiskRegister rather than
 * modifying an existing one (make it static).
 *
 * ------------------------------------------------------------
 * TODO 8 — importLegacyScanResults(String filename)
 *           throws IOException, InvalidRegisterDataException
 * ------------------------------------------------------------
 * LegacyScanOutputParser.java (provided, already written) parses one
 * line of an old scan dump into a Vulnerability. Read the given file
 * line by line, call LegacyScanOutputParser.parseLine(line) for each
 * non-blank line, and pass the result to addFinding() — the SAME
 * validation addFinding() already performs for everything else.
 * Even though this data comes from an internal, previously-tested
 * tool, it is still external input the moment it's read from a file,
 * and deserves the same scrutiny as anything else.
 * Wrap any exception from the parser or from addFinding() in
 * InvalidRegisterDataException so callers see one consistent
 * exception type from this method.
 * ============================================================
 */

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class RiskRegister {

    // TODO 1: declare your List<Vulnerability> and Set<String> fields here.


    public RiskRegister() {
        // TODO 1 (continued): initialize both fields here.

    }

    // TODO 2: validateCveId(String candidate)


    // TODO 3: validateComponent(String candidate)


    // TODO 4: addFinding(Vulnerability finding)


    // TODO 5: getFindings() and getUniqueCveIds()


    // TODO 6: saveToFile(String filename) throws IOException


    // TODO 7: static loadFromFile(String filename) throws IOException, InvalidRegisterDataException


    // TODO 8: importLegacyScanResults(String filename) throws IOException, InvalidRegisterDataException


}
