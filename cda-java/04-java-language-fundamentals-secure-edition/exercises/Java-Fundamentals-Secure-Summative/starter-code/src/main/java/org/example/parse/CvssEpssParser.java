package org.example.parse;

import org.example.model.CvssScore;
import org.example.model.EpssScore;
import org.example.util.InputValidator;
import org.example.util.ParseResult;
import org.example.util.SimpleJson;
import org.example.util.ValidationException;

import java.util.List;
import java.util.Map;

/**
 * Parses a combined CVSS + EPSS feed (JSON array of records, each with
 * cveId, cvssScore, severityLabel, epssProbability, epssPercentile) into
 * validated CvssScore/EpssScore pairs. Same trust-boundary and
 * fail-closed contract as the other *Parser classes.
 */
public final class CvssEpssParser {

    public static final class ScoredCve {
        public final CvssScore cvss;
        public final EpssScore epss;
        public ScoredCve(CvssScore cvss, EpssScore epss) {
            this.cvss = cvss;
            this.epss = epss;
        }
    }

    private static final List<String> ALLOWED_SEVERITY =
            List.of("NONE", "LOW", "MEDIUM", "HIGH", "CRITICAL");

    /**
     * TODO: same whole-document-vs-per-record structure as the other
     * three *Parser classes in this package.
     */
    public ParseResult<ScoredCve> parseScores(String jsonText) throws ValidationException {
        List<Object> records;

        try {
            records = SimpleJson.parseArray(jsonText);
        } catch (RuntimeException e) {
            throw new ValidationException("Invalid CVSS/EPSS feed", e);
        }

        ParseResult<ScoredCve> result = new ParseResult<>();

        for (int i = 0; i < records.size(); i++) {
            try {
                ScoredCve scoredCve = parseRecord(records.get(i));
                result.addAccepted(scoredCve);
            } catch (ValidationException e) {
                result.addRejected(i, e.getMessage());
            }
        }

        return result;
    }

    /**
     * TODO: parse a single raw JSON record into a validated ScoredCve.
     * Required steps, in order:
     *   1. Confirm rawRecord is a Map -- if not, throw.
     *   2. requireString(record, "cveId"), then
     *      InputValidator.validateCveId(...).
     *   3. requireDouble(record, "cvssScore"), then
     *      InputValidator.validateCvssScore(...).
     *   4. requireString(record, "severityLabel"), uppercase it, and
     *      check membership in ALLOWED_SEVERITY -- throw
     *      ValidationException naming the allow-list and the offending
     *      value if it's not a member. (Note: this allow-list check is
     *      inlined here rather than delegated to InputValidator, since
     *      severity labels are specific to this one parser -- unlike
     *      protocol/ecosystem/indicatorType, which are shared across
     *      multiple parsers and therefore live centrally in
     *      InputValidator.)
     *   5. requireDouble(record, "epssProbability"), then
     *      InputValidator.validateUnitInterval(..., "epssProbability").
     *   6. requireDouble(record, "epssPercentile"), then
     *      InputValidator.validateUnitInterval(..., "epssPercentile").
     *   7. Construct and return a new ScoredCve wrapping a new
     *      CvssScore(cveId, cvssScore, severityLabel) and a new
     *      EpssScore(cveId, epssProbability, epssPercentile).
     */
    private ScoredCve parseRecord(Object rawRecord) throws ValidationException {
        if (!(rawRecord instanceof Map)) {
            throw new ValidationException("Record must be a JSON object");
        }

        Map<String, Object> record = (Map<String, Object>) rawRecord;

        String cveId = requireString(record, "cveId");
        cveId = InputValidator.validateCveId(cveId);

        double cvssScore = requireDouble(record, "cvssScore");
        cvssScore = InputValidator.validateCvssScore(cvssScore);

        String severityLabel = requireString(record, "severityLabel");
        severityLabel = severityLabel.toUpperCase();

        if (!ALLOWED_SEVERITY.contains(severityLabel)) {
            throw new ValidationException(
                    "Severity label not in allow-list "
                            + ALLOWED_SEVERITY + ": '" + severityLabel + "'"
            );
        }

        double epssProbability = requireDouble(record, "epssProbability");
        epssProbability = InputValidator.validateUnitInterval(
                epssProbability,
                "epssProbability"
        );

        double epssPercentile = requireDouble(record, "epssPercentile");
        epssPercentile = InputValidator.validateUnitInterval(
                epssPercentile,
                "epssPercentile"
        );

        CvssScore cvss = new CvssScore(
                cveId,
                cvssScore,
                severityLabel
        );

        EpssScore epss = new EpssScore(
                cveId,
                epssProbability,
                epssPercentile
        );

        return new ScoredCve(cvss, epss);
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

    /**
     * TODO: return record.get(field) as a double, or throw
     * ValidationException naming the field if it is missing or not a
     * Number. Unlike requireInt() in the other parsers, this one does
     * NOT need to reject non-whole numbers -- CVSS/EPSS scores are
     * legitimately fractional.
     */
    private double requireDouble(Map<String, Object> record, String field)
            throws ValidationException {

        Object value = record.get(field);

        if (!(value instanceof Number)) {
            throw new ValidationException(
                    "Field '" + field + "' is missing or not a Number"
            );
        }

        return ((Number) value).doubleValue();
    }
}
