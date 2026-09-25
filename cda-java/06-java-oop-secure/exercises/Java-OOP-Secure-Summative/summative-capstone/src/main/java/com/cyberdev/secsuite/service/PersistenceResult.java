package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.ThreatIntelAlert;

import java.util.List;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Output of ThreatIntelCsvIngestionService.persistAndDeduplicate (SEC-11): which parsed alerts
 * were actually inserted, and which were skipped because an alert with the same
 * external_alert_id was already stored (the "logged" duplicates -- never thrown, never
 * upserted).
 */
public record PersistenceResult(List<ThreatIntelAlert> inserted, List<String> duplicateExternalAlertIds) {

    public PersistenceResult {
        if (inserted == null || duplicateExternalAlertIds == null) {
            throw new ValidationException("inserted and duplicateExternalAlertIds must not be null");
        }
        inserted = List.copyOf(inserted);
        duplicateExternalAlertIds = List.copyOf(duplicateExternalAlertIds);
    }
}
