package com.acme.reporting;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportingConfig {

    @Bean
    public DataExporter dataExporter(@Value("${report.exporter}") String exporter) {
        if ("remote".equalsIgnoreCase(exporter)) {
            return new RemoteScriptExporter();
        }
        return new CsvExporter();
    }

    @Bean
    public AuditLog auditLog() {
        return new NoOpAuditLog();
    }
}
