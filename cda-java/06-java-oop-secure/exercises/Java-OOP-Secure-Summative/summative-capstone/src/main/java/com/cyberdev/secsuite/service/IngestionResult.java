package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.ThreatIntelAlert;

import java.nio.file.Path;
import java.util.List;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Output of ThreatIntelCsvIngestionService.parseFile (SEC-10): every row of the file ends up in
 * exactly ONE of the two lists -- parsed (valid, ready to persist) or skipped (with a reason).
 * Invariant: parsed.size() + skipped.size() == dataRowsRead. Nothing has been written to any
 * repository yet; that is SEC-11's job.
 *
 * The lists are defensively copied into unmodifiable lists so a caller cannot mutate the
 * result after the fact.
 */
public record IngestionResult(Path source, int dataRowsRead, List<ThreatIntelAlert> parsed, List<SkippedRow> skipped) {

    public IngestionResult {
        if (source == null) {
            throw new ValidationException("source must not be null");
        }
        if (parsed == null || skipped == null) {
            throw new ValidationException("parsed and skipped must not be null");
        }
        parsed = List.copyOf(parsed);
        skipped = List.copyOf(skipped);
        if (parsed.size() + skipped.size() != dataRowsRead) {
            throw new ValidationException("every data row must be either parsed or skipped: "
                    + parsed.size() + " + " + skipped.size() + " != " + dataRowsRead);
        }
    }
}
