package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.ThreatModelEntry;

import java.util.List;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO (the Jdbc implementation is SEC-8).
 *
 * Persistence for the individual STRIDE entries of a threat model (table
 * "threat_model_entry"). See AnalystRepository for the shared contract.
 */
public interface ThreatModelEntryRepository {

    ThreatModelEntry save(ThreatModelEntry entry);

    /** Entries for one threat model, ordered by stride_category then description; empty if none. */
    List<ThreatModelEntry> findByThreatModelId(Long threatModelId);
}
