package com.acme.reporting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    private final ReportService reportService;
    private final RequestContext requestContext;

    public ReportController(ReportService reportService, RequestContext requestContext) {
        this.reportService = reportService;
        this.requestContext = requestContext;
    }

    @GetMapping("/reports/run")
    public String run(@RequestParam String user) {
        requestContext.setCurrentUser(user);
        return reportService.generate();
    }
}
