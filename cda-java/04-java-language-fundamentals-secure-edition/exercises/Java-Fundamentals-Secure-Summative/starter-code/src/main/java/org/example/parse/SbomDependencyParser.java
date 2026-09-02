package org.example.parse;

import org.example.model.Dependency;
import org.example.util.InputValidator;
import org.example.util.ParseResult;
import org.example.util.SimpleJson;
import org.example.util.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses the modern JSON SBOM feed (sbom_dependencies.json) into
 * validated Dependency objects. Same trust-boundary and fail-closed
 * contract as the other *Parser classes.
 *
 * NOTE: this is the MODERN JSON ingestion path. LegacyDependencyScanner
 * (in the legacy package) reads an older pipe-delimited log format from
 * a separate legacy scanning tool -- you are fixing that one's bugs
 * in-place, not replacing it with this class.
 */
public final class SbomDependencyParser {

    /**
     * TODO: same whole-document-vs-per-record structure as
     * ThreatIntelFeedParser.parseFeed(...) and PortScanResultParser.parseResults(...).
     */
    public ParseResult<Dependency> parseDependencies(String jsonText) throws ValidationException {
        List<Object> records;

        try {
            records = SimpleJson.parseArray(jsonText);
        } catch (RuntimeException e) {
            throw new ValidationException("Invalid SBOM dependency feed", e);
        }

        ParseResult<Dependency> result = new ParseResult<>();

        for (int i = 0; i < records.size(); i++) {
            try {
                Dependency dependency = parseRecord(records.get(i));
                result.addAccepted(dependency);
            } catch (ValidationException e) {
                result.addRejected(i, e.getMessage());
            }
        }

        return result;
    }

    /**
     * TODO: parse a single raw JSON record into a validated Dependency.
     * Required steps, in order:
     *   1. Confirm rawRecord is a Map -- if not, throw.
     *   2. requireString(record, "name"), then
     *      InputValidator.validateTextField(..., "name", 200).
     *   3. requireString(record, "version"), then
     *      InputValidator.validateTextField(..., "version", 100).
     *   4. requireString(record, "ecosystem"), then
     *      InputValidator.validateEcosystem(...).
     *   5. knownCves: read record.get("knownCves"). If null, use an
     *      empty list. Otherwise require it to be a List; require every
     *      element to be a String; validate each with
     *      InputValidator.validateCveId(...); collect into a new
     *      ArrayList<String>.
     *   6. Construct and return new Dependency(name, version, ecosystem, cves).
     */
    private Dependency parseRecord(Object rawRecord) throws ValidationException {
        if (!(rawRecord instanceof Map)) {
            throw new ValidationException("Record must be a JSON object");
        }

        Map<String, Object> record = (Map<String, Object>) rawRecord;

        String name = requireString(record, "name");
        name = InputValidator.validateTextField(name, "name", 200);

        String version = requireString(record, "version");
        version = InputValidator.validateTextField(version, "version", 100);

        String ecosystem = requireString(record, "ecosystem");
        ecosystem = InputValidator.validateEcosystem(ecosystem);

        List<String> cves = new ArrayList<>();

        Object rawCves = record.get("knownCves");

        if (rawCves != null) {
            if (!(rawCves instanceof List)) {
                throw new ValidationException(
                        "Field 'knownCves' must be a list"
                );
            }

            List<?> cveList = (List<?>) rawCves;

            for (Object cve : cveList) {
                if (!(cve instanceof String)) {
                    throw new ValidationException(
                            "Each knownCves entry must be a String"
                    );
                }

                String validatedCve = InputValidator.validateCveId(
                        (String) cve
                );

                cves.add(validatedCve);
            }
        }

        return new Dependency(
                name,
                version,
                ecosystem,
                cves
        );
    }

    /**
     * TODO: return record.get(field) cast to String, or throw
     * ValidationException naming the field if it is missing or not a String.
     */
    private String requireString(Map<String, Object> record, String field)
            throws ValidationException {

        Object value = record.get(field);

        if (!(value instanceof String)) {
            throw new ValidationException(
                    "Field '" + field + "' is missing or not a String"
            );
        }

        return (String) value;
    }
}
