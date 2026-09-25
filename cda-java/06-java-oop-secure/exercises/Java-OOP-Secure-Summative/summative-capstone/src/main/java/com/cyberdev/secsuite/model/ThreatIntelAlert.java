package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.time.Instant;
import java.util.Objects;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One threat intelligence alert (maps to "threat_intel_alert"), either seeded directly or
 * ingested from a CSV feed (SEC-10/SEC-11).
 *
 * Two identifiers, two jobs: {@code id} is OUR surrogate key; {@code externalAlertId} is the
 * FEED's identifier and is UNIQUE in the schema -- it is the deduplication key SEC-11 checks
 * before inserting, so ingesting the same feed twice cannot double the row count.
 *
 * Invariant (mirrored by a CHECK constraint in schema.sql): when indicatorType is CVE, the
 * indicator IS a CVE id, so relatedCveId must be non-null and equal to indicatorValue. For
 * IP/DOMAIN/FILE_HASH, relatedCveId is optional context ("this C2 IP is associated with
 * exploitation of CVE-X").
 *
 * ENTITY: equal by id (our surrogate key), not by externalAlertId, consistent with every other
 * entity in the model.
 */
public final class ThreatIntelAlert {

    private final Long id;
    private final String externalAlertId;
    private final String source;
    private final IndicatorType indicatorType;
    private final String indicatorValue;
    private final String relatedCveId;
    private final Severity severity;
    private final String description;
    private final Instant publishedAt;
    private final Instant ingestedAt;

    public ThreatIntelAlert(Long id, String externalAlertId, String source, IndicatorType indicatorType,
                            String indicatorValue, String relatedCveId, Severity severity, String description,
                            Instant publishedAt, Instant ingestedAt) {
        if (id == null) {
            throw new ValidationException("alert id must not be null");
        }
        if (externalAlertId == null || externalAlertId.isBlank()) {
            throw new ValidationException("externalAlertId must not be blank");
        }
        if (source == null || source.isBlank()) {
            throw new ValidationException("source must not be blank");
        }
        if (indicatorType == null) {
            throw new ValidationException("indicatorType must not be null");
        }
        if (indicatorValue == null || indicatorValue.isBlank()) {
            throw new ValidationException("indicatorValue must not be blank");
        }
        String normalizedRelatedCve = (relatedCveId == null || relatedCveId.isBlank()) ? null : relatedCveId.trim();
        if (normalizedRelatedCve != null && !CveCatalogEntry.isWellFormedCveId(normalizedRelatedCve)) {
            throw new ValidationException("relatedCveId must look like CVE-YYYY-NNNN, was '" + normalizedRelatedCve + "'");
        }
        if (indicatorType == IndicatorType.CVE
                && (normalizedRelatedCve == null || !normalizedRelatedCve.equals(indicatorValue.trim()))) {
            throw new ValidationException("a CVE indicator must carry the same CVE id in relatedCveId");
        }
        if (severity == null || severity == Severity.NONE) {
            throw new ValidationException("severity must be LOW, MEDIUM, HIGH or CRITICAL");
        }
        if (ingestedAt == null) {
            throw new ValidationException("ingestedAt must not be null");
        }
        this.id = id;
        this.externalAlertId = externalAlertId.trim();
        this.source = source.trim();
        this.indicatorType = indicatorType;
        this.indicatorValue = indicatorValue.trim();
        this.relatedCveId = normalizedRelatedCve;
        this.severity = severity;
        this.description = (description == null || description.isBlank()) ? null : description.trim();
        this.publishedAt = publishedAt;
        this.ingestedAt = ingestedAt;
    }

    public Long getId() {
        return id;
    }

    public ThreatIntelAlert withId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive");
        }
        return new ThreatIntelAlert(id, externalAlertId, source, indicatorType, indicatorValue, relatedCveId,
                severity, description, publishedAt, ingestedAt);
    }

    public String getExternalAlertId() {
        return externalAlertId;
    }

    public String getSource() {
        return source;
    }

    public IndicatorType getIndicatorType() {
        return indicatorType;
    }

    public String getIndicatorValue() {
        return indicatorValue;
    }

    /** May be null for non-CVE indicators. */
    public String getRelatedCveId() {
        return relatedCveId;
    }

    public Severity getSeverity() {
        return severity;
    }

    /** May be null. Untrusted feed text -- escape before rendering anywhere. */
    public String getDescription() {
        return description;
    }

    /** May be null. */
    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThreatIntelAlert other)) return false;
        return id != null && id > 0 && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ThreatIntelAlert{" + externalAlertId + " " + indicatorType + "=" + indicatorValue + " " + severity + "}";
    }
}
