package com.rti.integration.dto;

/**
 * Service-side request contract for fetching a report across the seam.
 */
public class ReportRequest {

    public String serviceId;
    public String accountId;
    public String filter;

    public ReportRequest() {
    }

    public ReportRequest(String serviceId, String accountId, String filter) {
        this.serviceId = serviceId;
        this.accountId = accountId;
        this.filter = filter;
    }
}
