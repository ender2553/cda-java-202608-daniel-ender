package com.acme.reporting;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ReportController {

    private final ReportService reportService;
    private final ExportService exportService;
    private final ScheduleService scheduleService;
    private final IngestService ingestService;

    public ReportController(ReportService reportService,
                            ExportService exportService,
                            ScheduleService scheduleService,
                            IngestService ingestService) {
        this.reportService = reportService;
        this.exportService = exportService;
        this.scheduleService = scheduleService;
        this.ingestService = ingestService;
    }

    // Suspected finding A: status comes straight from the client.
    @GetMapping("/reports")
    public List<Map<String, Object>> list(@RequestParam("status") String status) {
        return reportService.findByStatus(status);
    }

    // Suspected finding B: filename comes straight from the client.
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> export(@RequestParam("name") String name) throws Exception {
        return ResponseEntity.ok(exportService.readExport(name));
    }

    // Suspected finding C: the "period" param selects a report bucket.
    @GetMapping("/reports/rollup")
    public String rollup(@RequestParam("period") String period) throws Exception {
        return scheduleService.runRollup(period);
    }

    // Suspected finding D: an encoded blob is restored from the request body.
    @PostMapping("/reports/ingest")
    public String ingest(@RequestBody String payload) throws Exception {
        return ingestService.restore(payload);
    }

    // Suspected finding E: the sort column is provided by the client.
    @GetMapping("/reports/ranked")
    public List<Map<String, Object>> ranked(@RequestParam("sort") String sort) {
        return reportService.rankedBy(sort);
    }
}
