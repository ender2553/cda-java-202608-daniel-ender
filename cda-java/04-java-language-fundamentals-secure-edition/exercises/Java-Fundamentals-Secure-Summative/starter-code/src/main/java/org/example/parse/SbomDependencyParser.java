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
        throw new UnsupportedOperationException("TODO: implement parseDependencies()");
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
        throw new UnsupportedOperationException("TODO: implement parseRecord()");
    }

    /**
     * TODO: return record.get(field) cast to String, or throw
     * ValidationException naming the field if it is missing or not a String.
     */
    private String requireString(Map<String, Object> record, String field) throws ValidationException {
        throw new UnsupportedOperationException("TODO: implement requireString()");
    }
}
