package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Component;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.model.Severity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One SBOM component together with every catalog CVE linked to it -- the report-ready row that
 * SbomService.findVulnerableComponents (SEC-7) returns. cves is NEVER null: a component with no
 * known vulnerabilities has an EMPTY list, which the compact constructor enforces (null is
 * rejected rather than silently converted, so a bug that produces null is caught at the source).
 *
 * The CVE list is copied and sorted by CVSS score descending, then cve_id, so the worst issue
 * is always first regardless of the order the repository returned the links in.
 */
public record ComponentVulnerabilities(Component component, List<CveCatalogEntry> cves) {

    public ComponentVulnerabilities {
        if (component == null) {
            throw new ValidationException("component must not be null");
        }
        if (cves == null) {
            throw new ValidationException("cves must not be null -- use an empty list for 'no known vulnerabilities'");
        }
        cves = cves.stream()
                .sorted(Comparator.comparing(CveCatalogEntry::cvssScore).reversed()
                        .thenComparing(CveCatalogEntry::cveId))
                .toList();
    }

    public boolean isVulnerable() {
        return !cves.isEmpty();
    }

    /** The severity of the worst linked CVE, or empty if there are none. */
    public Optional<Severity> highestSeverity() {
        return cves.isEmpty() ? Optional.empty() : Optional.of(cves.get(0).severity());
    }
}
