package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.RiskRegisterEntry;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Persistence for the risk register (table "risk_register_entry"). See AnalystRepository for
 * the shared contract.
 */
public interface RiskRegisterRepository {

    RiskRegisterEntry save(RiskRegisterEntry entry);

    Optional<RiskRegisterEntry> findById(Long id);

    /** Every entry, ordered by risk_score descending, then title. */
    List<RiskRegisterEntry> findAll();

    /** The register entry raised from this scan finding, if one exists. */
    Optional<RiskRegisterEntry> findByScanFindingId(Long scanFindingId);
}
