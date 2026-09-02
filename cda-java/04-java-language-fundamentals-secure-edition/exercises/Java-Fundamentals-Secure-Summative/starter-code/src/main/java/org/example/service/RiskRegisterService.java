package org.example.service;

import org.example.model.CvssScore;
import org.example.model.Dependency;
import org.example.model.EpssScore;
import org.example.model.RiskRegisterEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core business logic: turns validated SBOM dependency data plus
 * CVSS/EPSS scores into a prioritized risk register.
 *
 * USER STORY: As a security analyst, I need every vulnerable dependency
 * turned into a risk register entry with a severity that reflects BOTH
 * how bad the vulnerability could be (CVSS) and how likely it is to
 * actually be exploited in the wild (EPSS) -- a CVSS 7.5 with a 90%
 * EPSS probability is a more urgent problem than a CVSS 9.8 that has
 * never been seen exploited, and the severity classification should
 * reflect that nuance rather than relying on CVSS alone.
 */
public final class RiskRegisterService {

    private static final List<String> SEVERITY_RANK =
            List.of("LOW", "MEDIUM", "HIGH", "CRITICAL", "UNSCORED");

    /**
     * TODO: build one RiskRegisterEntry per (dependency, CVE) pair for
     * every dependency that has at least one known CVE.
     *
     * For each dependency, for each CVE in dep.getKnownCves():
     *   - Look up CvssScore in cvssByCve and EpssScore in epssByCve by
     *     CVE ID (either or both may be absent -- external feeds don't
     *     always line up perfectly).
     *   - If cvss is null: severity = "UNSCORED", cvssValue = -1.0
     *     (a sentinel OUTSIDE the normal [0.0, 10.0] range -- an unscored
     *     CVE must never look identical to a confirmed-low-severity one
     *     that happens to have a real 0.0-ish score), epssValue = the
     *     matching epss probability if present, else -1.0.
     *   - If cvss is present: epssValue = the matching epss probability
     *     if present, else 0.0 (a confirmed CVSS score with no EPSS data
     *     is treated as "no known exploitation signal yet", which is
     *     different from "we know nothing about this CVE at all" --
     *     that's the UNSCORED case above). cvssValue = cvss.getBaseScore().
     *     severity = classifySeverity(cvssValue, epssValue).
     *   - riskId: String.format("RISK-%04d", counter++) using a counter
     *     you increment starting at 1.
     *   - description: String.format("%s@%s (%s) is vulnerable to %s",
     *     dep.getName(), dep.getVersion(), dep.getEcosystem(), cveId).
     *   - Construct a new RiskRegisterEntry(riskId,
     *     dep.getName() + "@" + dep.getVersion(), description, severity,
     *     cvssValue, epssValue, List.of(cveId)) and add it to the result list.
     */
    public List<RiskRegisterEntry> buildRegister(List<Dependency> dependencies,
                                                  Map<String, CvssScore> cvssByCve,
                                                  Map<String, EpssScore> epssByCve) {
        throw new UnsupportedOperationException("TODO: implement buildRegister()");
    }

    /**
     * TODO: classify severity using BOTH CVSS base score and EPSS exploit
     * probability. A high EPSS probability escalates a moderate CVSS
     * score, reflecting real-world exploitation likelihood rather than
     * theoretical severity alone.
     *
     *  CRITICAL: cvss >= 9.0, OR (cvss >= 7.0 AND epss >= 0.50)
     *  HIGH:     cvss >= 7.0, OR (cvss >= 4.0 AND epss >= 0.50)
     *  MEDIUM:   cvss >= 4.0
     *  LOW:      everything else
     *
     * All threshold comparisons are INCLUSIVE (>=) at the stated boundary
     * value -- e.g. cvss exactly 9.0 is CRITICAL, epss exactly 0.50
     * counts as meeting the escalation condition. Check CRITICAL's
     * condition first, then HIGH's, then MEDIUM's, falling through to
     * LOW -- the order matters because the conditions are NOT mutually
     * exclusive (e.g. cvss=9.5 satisfies both the CRITICAL and HIGH
     * cvss-alone thresholds).
     */
    public String classifySeverity(double cvssScore, double epssProbability) {
        throw new UnsupportedOperationException("TODO: implement classifySeverity()");
    }

    public List<RiskRegisterEntry> filterByStatus(List<RiskRegisterEntry> register, String status) {
        return register.stream()
                .filter(r -> r.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    /**
     * REFERENCE EXAMPLE -- fully implemented for you. Highest-priority
     * risks first: severity rank descending (per SEVERITY_RANK above),
     * then CVSS score descending as a tiebreaker.
     */
    public List<RiskRegisterEntry> topRisks(List<RiskRegisterEntry> register, int n) {
        return register.stream()
                .sorted(Comparator
                        .comparingInt((RiskRegisterEntry r) -> SEVERITY_RANK.indexOf(r.getSeverity())).reversed()
                        .thenComparing(Comparator.comparingDouble(RiskRegisterEntry::getCvssScore).reversed()))
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * TODO: build a Map<String, Long> with an entry for EVERY severity
     * bucket in the fixed order "CRITICAL", "HIGH", "MEDIUM", "LOW",
     * "UNSCORED" -- even buckets with zero entries must appear with a
     * count of 0L, so the executive report always shows a complete
     * breakdown rather than silently omitting empty categories. Then walk
     * `register` and increment the count for each entry's severity
     * (Map.merge(...) with Long::sum is a convenient way to do this).
     */
    public Map<String, Long> countBySeverity(List<RiskRegisterEntry> register) {
        throw new UnsupportedOperationException("TODO: implement countBySeverity()");
    }
}
