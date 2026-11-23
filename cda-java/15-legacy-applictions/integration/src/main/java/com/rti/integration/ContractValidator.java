package com.rti.integration;

import com.rti.integration.dto.ReportRequest;

/**
 * Validates an inbound request against the seam contract (schema / allow-list).
 */
public interface ContractValidator {

    void validate(ReportRequest req);
}
