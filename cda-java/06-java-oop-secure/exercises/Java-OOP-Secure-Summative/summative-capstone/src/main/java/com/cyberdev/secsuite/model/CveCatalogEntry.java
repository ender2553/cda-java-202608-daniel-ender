package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One published vulnerability in the local CVE catalog (maps to "cve_catalog"). This is the
 * SINGLE place CVE metadata (description, CVSS score) lives in the whole schema: scan
 * findings, SBOM component links and threat intel alerts all reference it by cve_id instead
 * of copying the description/score -- that is the 3NF point of the table.
 *
 * INSTRUCTOR NOTE -- VALUE equality, on purpose (the Money side of the Merchant/Money
 * contrast): a catalog entry is an immutable snapshot of published facts, not a thing with a
 * lifecycle we track. If the upstream database re-scores CVE-2023-91004 from 7.0 to 7.5, the
 * old and new entries are genuinely DIFFERENT values, and a record's generated
 * equals()/hashCode() over all components says exactly that. Contrast Asset, whose identity
 * survives a change to its fields.
 *
 * The score is normalized to scale 1 (matching NUMERIC(3,1)) in the compact constructor, so
 * "9" and "9.0" produce EQUAL records -- BigDecimal.equals is scale-sensitive, and without
 * this normalization two logically identical entries would compare unequal. A score with more
 * than one decimal place is rejected rather than silently rounded.
 */
public record CveCatalogEntry(String cveId, String description, BigDecimal cvssScore) {

    /** CVE-YYYY-NNNN... (four-digit year, four or more digit sequence number). */
    public static final Pattern CVE_ID_PATTERN = Pattern.compile("^CVE-\\d{4}-\\d{4,7}$");

    public CveCatalogEntry {
        if (cveId == null || !CVE_ID_PATTERN.matcher(cveId.trim()).matches()) {
            throw new ValidationException("cveId must look like CVE-YYYY-NNNN, was '" + cveId + "'");
        }
        cveId = cveId.trim();
        if (description == null || description.isBlank()) {
            throw new ValidationException("description must not be blank for " + cveId);
        }
        description = description.trim();
        if (cvssScore == null) {
            throw new ValidationException("cvssScore must not be null for " + cveId);
        }
        try {
            cvssScore = cvssScore.setScale(1, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new ValidationException("cvssScore must have at most one decimal place, was " + cvssScore, e);
        }
        if (cvssScore.compareTo(BigDecimal.ZERO) < 0 || cvssScore.compareTo(BigDecimal.TEN) > 0) {
            throw new ValidationException("cvssScore must be between 0.0 and 10.0, was " + cvssScore);
        }
    }

    /** Derived, never stored -- see the design note on Severity. */
    public Severity severity() {
        return Severity.fromCvssScore(cvssScore.doubleValue());
    }

    public static boolean isWellFormedCveId(String candidate) {
        return candidate != null && CVE_ID_PATTERN.matcher(candidate).matches();
    }
}
