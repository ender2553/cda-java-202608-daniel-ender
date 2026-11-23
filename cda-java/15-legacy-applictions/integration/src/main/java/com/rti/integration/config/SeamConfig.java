package com.rti.integration.config;

import java.util.Set;

/**
 * Least-privilege credential and scope holder for a single seam consumer.
 */
public class SeamConfig {

    public final String serviceId;
    public final String sharedSecret;
    public final Set<String> allowedFields;

    public SeamConfig(String serviceId, String sharedSecret, Set<String> allowedFields) {
        this.serviceId = serviceId;
        this.sharedSecret = sharedSecret;
        this.allowedFields = allowedFields;
    }
}
