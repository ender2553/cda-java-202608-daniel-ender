package org.example.parse;

import org.example.model.ThreatIndicator;
import org.example.util.InputValidator;
import org.example.util.ParseResult;
import org.example.util.SimpleJson;
import org.example.util.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses a simulated external threat intelligence feed (JSON array of
 * indicator objects) into validated ThreatIndicator domain objects.
 *
 * TRUST BOUNDARY: the JSON text handed to parseFeed(...) is UNTRUSTED --
 * it simulates data pulled from a third-party feed over the network.
 * SimpleJson.parse(...) only tells you the data is well-formed JSON; it
 * says NOTHING about whether the values inside make sense or are safe.
 * Every field must go through InputValidator before it is allowed to
 * become part of a ThreatIndicator.
 *
 * FAIL-CLOSED CONTRACT:
 *  - If the JSON text is not even a valid JSON array (e.g. truncated,
 *    not JSON at all, or a JSON object instead of an array), the ENTIRE
 *    feed is rejected by throwing ValidationException -- there is no
 *    reasonable partial interpretation of a corrupted top-level document.
 *  - If an individual record within a valid array is missing a field,
 *    has a field of the wrong type, or fails InputValidator, that ONE
 *    record is rejected and recorded in the ParseResult; parsing
 *    continues with the remaining records. One bad indicator must never
 *    take down ingestion of the whole feed.
 *  - Values are never coerced or defaulted. A missing "confidence" field
 *    is a rejected record, not a record with confidence=0.
 */
public final class ThreatIntelFeedParser {

    /**
     * TODO (WHOLE-DOCUMENT REJECTION): call SimpleJson.parseArray(jsonText)
     * inside a try block. If it throws a RuntimeException (SimpleJson's
     * JsonParseException, or a ClassCastException from a malformed
     * top-level shape), re-throw it wrapped as a ValidationException
     * (constructor that takes a message + cause) so the WHOLE feed is
     * rejected -- do not attempt to salvage a partial array from a
     * corrupted document.
     *
     * TODO (PER-RECORD REJECTION): once you have the raw List<Object> of
     * records, loop over it by index. For each record, call
     * parseRecord(...) (see below) inside a try/catch for
     * ValidationException; on success call result.addAccepted(...), on
     * failure call result.addRejected(index, e.getMessage()) and move on
     * to the next record -- never let one bad record stop the loop or
     * propagate out of this method.
     */
    public ParseResult<ThreatIndicator> parseFeed(String jsonText) throws ValidationException {
        List<Object> records;

        try {
            records = SimpleJson.parseArray(jsonText);
        } catch (RuntimeException e) {
            throw new ValidationException("Invalid threat intelligence feed", e);
        }

        ParseResult<ThreatIndicator> result = new ParseResult<>();

        for (int i = 0; i < records.size(); i++) {
            try {
                ThreatIndicator indicator = parseRecord(records.get(i));
                result.addAccepted(indicator);
            } catch (ValidationException e) {
                result.addRejected(i, e.getMessage());
            }
        }

        return result;
    }

    /**
     * TODO: parse a single raw JSON record into a validated
     * ThreatIndicator, or throw ValidationException if it fails any
     * check. Required steps, in order:
     *   1. Confirm rawRecord is a Map (JSON object) -- if not, throw.
     *   2. requireString(record, "indicatorType"), then
     *      InputValidator.validateIndicatorType(...).
     *   3. requireString(record, "indicatorValue"), then
     *      InputValidator.validateIndicatorValue(indicatorType, ...) --
     *      note this call depends on the ALREADY-VALIDATED indicatorType
     *      from step 2, since the value's expected format depends on the
     *      type.
     *   4. requireInt(record, "confidence"), then
     *      InputValidator.validateConfidence(...).
     *   5. requireString(record, "source"), then
     *      InputValidator.validateTextField(..., "source", 200).
     *   6. tags: read record.get("tags"). If null, use an empty list.
     *      Otherwise require it to be a List; require every element to
     *      be a String; validate each with
     *      InputValidator.validateTextField(..., "tag", 50); collect into
     *      a new ArrayList<String>.
     *   7. Construct and return new ThreatIndicator(indicatorValue,
     *      indicatorType, confidence, source, tags).
     */
    private ThreatIndicator parseRecord(Object rawRecord) throws ValidationException {
        if (!(rawRecord instanceof Map)) {
            throw new ValidationException("Record must be a JSON object");
        }

        Map<String, Object> record = (Map<String, Object>) rawRecord;

        String indicatorType = requireString(record, "indicatorType");
        indicatorType = InputValidator.validateIndicatorType(indicatorType);

        String indicatorValue = requireString(record, "indicatorValue");
        indicatorValue = InputValidator.validateIndicatorValue(
                indicatorType, indicatorValue
        );

        int confidence = requireInt(record, "confidence");
        confidence = InputValidator.validateConfidence(confidence);

        String source = requireString(record, "source");
        source = InputValidator.validateTextField(source, "source", 200);

        List<String> tags = new ArrayList<>();

        Object rawTags = record.get("tags");

        if (rawTags != null) {
            if (!(rawTags instanceof List)) {
                throw new ValidationException("Field 'tags' must be a list");
            }

            List<?> tagList = (List<?>) rawTags;

            for (Object tag : tagList) {
                if (!(tag instanceof String)) {
                    throw new ValidationException("Each tag must be a String");
                }

                String validatedTag =
                        InputValidator.validateTextField((String) tag, "tag", 50);

                tags.add(validatedTag);
            }
        }

        return new ThreatIndicator(
                indicatorValue,
                indicatorType,
                confidence,
                source,
                tags
        );
    }

    /**
     * TODO: return record.get(field) cast to String, or throw
     * ValidationException naming the field if it is missing or not a
     * String.
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
     * TODO: return record.get(field) as an int, or throw
     * ValidationException naming the field if it is missing, not a
     * Number, or not a whole number (SimpleJson parses ALL JSON numbers
     * as Double, so "42" and "42.7" both arrive here as a Double -- you
     * must reject the latter rather than truncating it, since silently
     * truncating 42.7 to 42 would be a coercion, not a validation).
     */
    private int requireInt(Map<String, Object> record, String field)
            throws ValidationException {

        Object value = record.get(field);

        if (!(value instanceof Number)) {
            throw new ValidationException(
                    "Field '" + field + "' is missing or not a number"
            );
        }

        double number = ((Number) value).doubleValue();

        if (Double.isNaN(number) || number != Math.floor(number)) {
            throw new ValidationException(
                    "Field '" + field + "' must be a whole number"
            );
        }

        if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
            throw new ValidationException(
                    "Field '" + field + "' is outside the integer range"
            );
        }

        return (int) number;
    }
}
