package com.cyberdev.secsuite.service;

import org.springframework.stereotype.Service;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.repository.AssetRepository;
import com.cyberdev.secsuite.repository.ScanFindingRepository;
import com.cyberdev.secsuite.repository.ThreatIntelAlertRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The threat intelligence alert viewer: lists alerts and correlates them with the scanner's
 * open findings.
 */
@Service
public class ThreatIntelAlertService {

    private final ThreatIntelAlertRepository alertRepository;
    private final ScanFindingRepository findingRepository;
    private final AssetRepository assetRepository;

    public ThreatIntelAlertService(ThreatIntelAlertRepository alertRepository, ScanFindingRepository findingRepository,
                                   AssetRepository assetRepository) {
        if (alertRepository == null || findingRepository == null || assetRepository == null) {
            throw new ValidationException("ThreatIntelAlertService dependencies must not be null");
        }
        this.alertRepository = alertRepository;
        this.findingRepository = findingRepository;
        this.assetRepository = assetRepository;
    }

    /** GIVEN -- every alert (seeded + ingested), ordered by external_alert_id. */
    public List<ThreatIntelAlert> listAlerts() {
        return alertRepository.findAll();
    }

    // INSTRUCTOR NOTE [SEC-12]: Concept tested: a correlation with ZERO false positives. For
    // every alert whose indicatorType is CVE (alertRepository.findByIndicatorType(CVE) -- not
    // findAll()), look up the OPEN findings for its relatedCveId
    // (findingRepository.findOpenByCveId) and emit one CorrelationHit(asset, alert, finding)
    // per matching finding (one alert can hit several assets). Resolve each finding's asset
    // through assetRepository and fail closed (ValidationException) if it is missing. Sort the
    // result deterministically: hostname, then external alert id. Two classes of false
    // positive the seed data is built to catch: (1) TI-SEED-002's CVE only matches a RESOLVED
    // finding -- a patched host is not an active hit; (2) TI-SEED-005 (FILE_HASH) and the
    // feed's FILE_HASH row carry a relatedCveId that DOES have an open finding -- but a
    // malware hash "associated with" a CVE is context, not evidence that this host is
    // targeted, so non-CVE indicators must never produce a hit. Common mistakes: iterating
    // findAll() and matching on relatedCveId regardless of indicator type; using findAll() on
    // findings and forgetting the OPEN filter; comparing indicatorValue to the CVE for
    // non-CVE types; emitting only the FIRST matching asset per alert.
    // SECURITY CALLOUT: alert fatigue is a security failure mode. Every false positive in this
    // list trains the analyst reading it to trust the list a little less, and the true
    // positive gets ignored along with the noise.
    public List<CorrelationHit> correlateWithFindings() {
        throw new UnsupportedOperationException(
                "TODO [SEC-12]: CVE-type alerts x OPEN findings only, one hit per matching asset, sorted by hostname then alert id");
    }
}
