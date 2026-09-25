package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.model.ThreatIntelAlert;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One ACTIVE threat-intel correlation (SEC-12): a CVE-type alert whose CVE matches an OPEN scan
 * finding on an asset -- "the thing the feed says is being exploited is currently unpatched on
 * this host". The compact constructor re-checks the invariants, so a false positive built from
 * a RESOLVED finding or a non-CVE indicator cannot even be constructed.
 */
public record CorrelationHit(Asset asset, ThreatIntelAlert alert, ScanFinding finding) {

    public CorrelationHit {
        if (asset == null || alert == null || finding == null) {
            throw new ValidationException("asset, alert and finding must not be null");
        }
        if (!finding.isOpen()) {
            throw new ValidationException("a correlation hit requires an OPEN finding");
        }
        if (!finding.getAssetId().equals(asset.getId())) {
            throw new ValidationException("finding does not belong to asset " + asset.getHostname());
        }
        if (alert.getRelatedCveId() == null || !alert.getRelatedCveId().equals(finding.getCveId())) {
            throw new ValidationException("alert CVE does not match finding CVE");
        }
    }
}
