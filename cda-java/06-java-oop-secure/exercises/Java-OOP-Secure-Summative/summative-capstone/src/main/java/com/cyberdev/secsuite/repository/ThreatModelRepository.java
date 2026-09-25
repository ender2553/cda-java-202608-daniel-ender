package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.ThreatModel;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Persistence for threat models (table "threat_model"). See AnalystRepository for the shared
 * contract.
 */
public interface ThreatModelRepository {

    ThreatModel save(ThreatModel threatModel);

    Optional<ThreatModel> findById(Long id);

    /** Every threat model, ordered by title. */
    List<ThreatModel> findAll();

    List<ThreatModel> findByAssetId(Long assetId);
}
