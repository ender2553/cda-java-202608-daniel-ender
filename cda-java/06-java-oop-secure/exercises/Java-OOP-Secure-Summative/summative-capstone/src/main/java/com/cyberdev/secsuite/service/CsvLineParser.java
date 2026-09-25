package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Splits ONE line of RFC 4180-style CSV into fields: comma-separated; a field may be wrapped in
 * double quotes, in which case it may contain commas, and a literal quote is written as two
 * quotes (""). Fields are returned exactly as written (no trimming -- validation decides).
 *
 * Deliberate limitation (fail closed): quoted fields may NOT span multiple lines. A line that
 * ends inside an open quote, or that has a stray quote inside an unquoted field, is rejected
 * with a ValidationException -- the caller (SEC-10) records that row as skipped and moves on.
 * Guessing where a broken quote "probably" ends is how one malformed row silently eats the
 * next ten.
 *
 * INSTRUCTOR NOTE: given rather than a TODO so that SEC-10's graded work is the VALIDATION of
 * untrusted fields, not re-implementing a tokenizer. Why not `line.split(",")`? Because the
 * feed's free-text description column legitimately contains commas.
 */
public final class CsvLineParser {

    private CsvLineParser() {
    }

    public static List<String> parse(String line) {
        if (line == null) {
            throw new ValidationException("line must not be null");
        }
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean fieldWasQuoted = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i += 2;
                        continue;
                    }
                    inQuotes = false;
                    i++;
                    // after a closing quote only a comma or end-of-line is legal
                    if (i < line.length() && line.charAt(i) != ',') {
                        throw new ValidationException("unexpected character after closing quote at column " + (i + 1));
                    }
                    continue;
                }
                current.append(c);
                i++;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
                fieldWasQuoted = false;
                i++;
            } else if (c == '"') {
                if (current.length() > 0 || fieldWasQuoted) {
                    throw new ValidationException("stray quote inside an unquoted field at column " + (i + 1));
                }
                inQuotes = true;
                fieldWasQuoted = true;
                i++;
            } else {
                current.append(c);
                i++;
            }
        }
        if (inQuotes) {
            throw new ValidationException("unterminated quoted field (multi-line fields are not supported)");
        }
        fields.add(current.toString());
        return fields;
    }
}
