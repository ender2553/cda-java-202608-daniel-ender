package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ValidationException;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One complete ingestion run (parse, then persist) of one CSV file -- what the report's
 * "Threat Intel CSV Ingestion Summary" section (SEC-16) renders, one row per run.
 */
public record IngestionSummary(IngestionResult parseResult, PersistenceResult persistenceResult) {

    public IngestionSummary {
        if (parseResult == null || persistenceResult == null) {
            throw new ValidationException("parseResult and persistenceResult must not be null");
        }
    }
}
