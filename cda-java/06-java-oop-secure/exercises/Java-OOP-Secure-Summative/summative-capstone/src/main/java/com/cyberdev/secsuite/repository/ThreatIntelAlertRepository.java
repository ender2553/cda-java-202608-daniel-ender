package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.ThreatIntelAlert;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO (the Jdbc implementation is SEC-9).
 *
 * Persistence for threat intelligence alerts (table "threat_intel_alert"). See
 * AnalystRepository for the shared contract.
 */
public interface ThreatIntelAlertRepository {

    ThreatIntelAlert save(ThreatIntelAlert alert);

    /** Lookup by the FEED's id -- the deduplication key for SEC-11. */
    Optional<ThreatIntelAlert> findByExternalAlertId(String externalAlertId);

    /** Every alert, ordered by external_alert_id. */
    List<ThreatIntelAlert> findAll();

    /** Alerts of one indicator type, ordered by external_alert_id. */
    List<ThreatIntelAlert> findByIndicatorType(IndicatorType indicatorType);

    long count();
}
