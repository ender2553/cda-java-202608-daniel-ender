package org.example.report;

import org.example.model.RiskRegisterEntry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates everything the executive report needs to show. Built up by
 * SecurityAnalystConsole as it ingests each data feed.
 *
 * PROVIDED FOR YOU -- a plain data holder, no security logic lives here.
 */
public final class ExecutiveReportData {

    public static final class FeedIngestionStats {
        public final String feedName;
        public final int acceptedCount;
        public final int rejectedCount;
        public final List<String> sampleRejectionReasons;

        public FeedIngestionStats(String feedName, int acceptedCount, int rejectedCount,
                                   List<String> sampleRejectionReasons) {
            this.feedName = feedName;
            this.acceptedCount = acceptedCount;
            this.rejectedCount = rejectedCount;
            this.sampleRejectionReasons = sampleRejectionReasons;
        }
    }

    private final List<FeedIngestionStats> ingestionStats = new ArrayList<>();
    private Map<String, Long> severityCounts = new LinkedHashMap<>();
    private List<RiskRegisterEntry> topRisks = new ArrayList<>();
    private int totalDependencies;
    private int vulnerableDependencies;

    public void addIngestionStats(FeedIngestionStats stats) {
        ingestionStats.add(stats);
    }

    public List<FeedIngestionStats> getIngestionStats() { return ingestionStats; }

    public void setSeverityCounts(Map<String, Long> severityCounts) { this.severityCounts = severityCounts; }
    public Map<String, Long> getSeverityCounts() { return severityCounts; }

    public void setTopRisks(List<RiskRegisterEntry> topRisks) { this.topRisks = topRisks; }
    public List<RiskRegisterEntry> getTopRisks() { return topRisks; }

    public void setTotalDependencies(int totalDependencies) { this.totalDependencies = totalDependencies; }
    public int getTotalDependencies() { return totalDependencies; }

    public void setVulnerableDependencies(int vulnerableDependencies) { this.vulnerableDependencies = vulnerableDependencies; }
    public int getVulnerableDependencies() { return vulnerableDependencies; }
}
