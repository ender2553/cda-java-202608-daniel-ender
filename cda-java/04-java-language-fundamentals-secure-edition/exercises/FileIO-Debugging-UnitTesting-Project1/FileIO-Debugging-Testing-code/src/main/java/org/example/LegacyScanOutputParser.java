package org.example;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Parses output from the old scanner tool the security team used before
 * this year's toolkit existed. Several archived scan dumps still need
 * to be imported into the new Risk Register.
 *
 * Legacy line format (comma-separated):
 *   cveId,component,version,severityLetter
 * Example:
 *   CVE-2023-5555,log4j-core,2.14.1,H
 *
 * This class has already been written and tested against the archived
 * scan files — you should not need to modify it for this lab.
 */
public class LegacyScanOutputParser {

    public static Vulnerability parseLine(String rawLine) {
        String[] parts = rawLine.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Malformed legacy scan line: " + rawLine);
        }

        String cveId = parts[0].strip();
        String component = parts[1].strip();
        String version = parts[2].strip();
        Severity severity = mapSeverityLetter(parts[3].strip());

        // The legacy tool never tracked cost or discovery date, so
        // imported findings get sensible defaults for those fields.
        return new Vulnerability(cveId, component, version, severity, BigDecimal.ZERO, LocalDate.now());
    }

    private static Severity mapSeverityLetter(String letter) {
        switch (letter.toUpperCase()) {
            case "C":
                return Severity.CRITICAL;
            case "H":
                return Severity.HIGH;
            case "M":
                return Severity.MEDIUM;
            case "L":
                return Severity.LOW;
            default:
                throw new IllegalArgumentException("Unknown severity letter: " + letter);
        }
    }
}
