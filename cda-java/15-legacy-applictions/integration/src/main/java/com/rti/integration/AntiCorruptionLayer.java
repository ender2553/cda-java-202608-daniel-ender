package com.rti.integration;

import com.rti.integration.dto.ServiceReport;

import java.util.Map;

/**
 * Translates a legacy account summary into the service-side model so that
 * no legacy field shape leaks past the seam.
 */
public interface AntiCorruptionLayer {

    ServiceReport toServiceModel(Map<String, Object> legacySummary);
}
