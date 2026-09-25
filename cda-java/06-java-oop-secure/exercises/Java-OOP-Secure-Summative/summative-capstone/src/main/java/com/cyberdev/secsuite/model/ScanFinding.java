package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.time.Instant;
import java.util.Objects;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One vulnerability observed on one asset by the scanner (maps to "scan_finding"). Note what
 * is NOT here: no CVE description, no CVSS score, no severity, no hostname. The finding holds
 * only foreign keys (assetId, cveId) plus facts about the OBSERVATION itself (port, service,
 * when, status) -- everything else is looked up through the key, which is the 3NF discipline
 * of the schema carried into the object model.
 *
 * ENTITY: equal by id. Immutable; {@link #withStatus(FindingStatus)} returns a copy.
 */
public final class ScanFinding {

    private final Long id;
    private final Long assetId;
    private final String cveId;
    private final Integer port;
    private final String serviceName;
    private final Instant detectedAt;
    private final FindingStatus status;

    public ScanFinding(Long id, Long assetId, String cveId, Integer port, String serviceName,
                       Instant detectedAt, FindingStatus status) {
        if (id == null) {
            throw new ValidationException("finding id must not be null");
        }
        if (assetId == null) {
            throw new ValidationException("assetId must not be null");
        }
        if (!CveCatalogEntry.isWellFormedCveId(cveId)) {
            throw new ValidationException("cveId must look like CVE-YYYY-NNNN, was '" + cveId + "'");
        }
        if (port != null && (port < 0 || port > 65535)) {
            throw new ValidationException("port must be between 0 and 65535, was " + port);
        }
        if (detectedAt == null) {
            throw new ValidationException("detectedAt must not be null");
        }
        if (status == null) {
            throw new ValidationException("status must not be null");
        }
        this.id = id;
        this.assetId = assetId;
        this.cveId = cveId;
        this.port = port;
        this.serviceName = (serviceName == null || serviceName.isBlank()) ? null : serviceName.trim();
        this.detectedAt = detectedAt;
        this.status = status;
    }

    public ScanFinding withStatus(FindingStatus newStatus) {
        return new ScanFinding(id, assetId, cveId, port, serviceName, detectedAt, newStatus);
    }

    public boolean isOpen() {
        return status == FindingStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public ScanFinding withId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive");
        }
        return new ScanFinding(id, assetId, cveId, port, serviceName, detectedAt, status);
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getCveId() {
        return cveId;
    }

    /** May be null (e.g. a host-level finding with no listening port). */
    public Integer getPort() {
        return port;
    }

    /** May be null. */
    public String getServiceName() {
        return serviceName;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public FindingStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanFinding other)) return false;
        return id != null && id > 0 && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ScanFinding{" + cveId + " on asset " + assetId + " port=" + port + " " + status + "}";
    }
}
