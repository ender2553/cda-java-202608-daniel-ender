package com.acme.portal;

import org.springframework.stereotype.Service;

@Service
public class InternalAuditService {

    public void record(String actor, String action) {
        // Appends a tamper-evident entry to the internal audit trail.
    }

    public void purgeTrail(String userId) {
        // Privileged: removes audit-trail entries for a user.
    }

    public String exportTrail() {
        // Privileged: exports the full audit trail.
        return "";
    }
}
