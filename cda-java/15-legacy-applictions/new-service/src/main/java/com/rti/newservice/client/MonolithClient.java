package com.rti.newservice.client;

import com.rti.integration.dto.ServiceReport;
import org.springframework.stereotype.Component;

/**
 * Client that fetches account summaries from the monolith across the seam.
 *
 * These methods are not yet wired to the secured seam. Across the course you will
 * route them through the integration module (SeamAuthenticator, ContractValidator,
 * AntiCorruptionLayer) using least-privilege credentials.
 */
@Component
public class MonolithClient {

    public ServiceReport fetchReport(String accountId) {
        // TODO seam: authenticate via SeamAuthenticator, validate via ContractValidator,
        // call the monolith /reports/{accountId}, then map via AntiCorruptionLayer.
        throw new UnsupportedOperationException("TODO: wire MonolithClient through the secured seam");
    }

    public ServiceReport search(String filter) {
        // TODO seam: authenticate, validate the filter against the contract allow-list,
        // call the monolith /reports/search, then map via AntiCorruptionLayer.
        throw new UnsupportedOperationException("TODO: wire MonolithClient search through the secured seam");
    }
}
