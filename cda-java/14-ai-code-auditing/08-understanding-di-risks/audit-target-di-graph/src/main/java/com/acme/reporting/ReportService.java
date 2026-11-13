package com.acme.reporting;

import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final DataExporter exporter;
    private final AuditLog auditLog;
    private final RequestContext requestContext;

    public ReportService(DataExporter exporter, AuditLog auditLog, RequestContext requestContext) {
        this.exporter = exporter;
        this.auditLog = auditLog;
        this.requestContext = requestContext;
    }

    public String generate() {
        String user = requestContext.getCurrentUser();
        auditLog.record("report generated for " + user);
        return exporter.export("report-data for " + user);
    }
}
