package com.rti.legacy.reporting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * HTTP entry point for reports. This is the seam the new service calls.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/{accountId}")
    public Map<String, Object> summary(@PathVariable String accountId) {
        return reports.accountSummary(accountId);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String filter) {
        return reports.search(filter);
    }
}
