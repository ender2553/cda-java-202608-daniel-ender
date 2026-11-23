package com.rti.integration.stubs;

import com.rti.integration.ContractValidator;
import com.rti.integration.dto.ReportRequest;

/**
 * Unbuilt placeholder. Real allow-list contract validation is authored during the course.
 */
public class StubContractValidator implements ContractValidator {

    @Override
    public void validate(ReportRequest req) {
        throw new UnsupportedOperationException("TODO: validate the request against the seam contract");
    }
}
