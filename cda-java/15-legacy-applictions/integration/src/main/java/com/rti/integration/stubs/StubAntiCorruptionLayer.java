package com.rti.integration.stubs;

import com.rti.integration.AntiCorruptionLayer;
import com.rti.integration.dto.ServiceReport;

import java.util.Map;

/**
 * Unbuilt placeholder. Real legacy-to-service translation is authored during the course.
 */
public class StubAntiCorruptionLayer implements AntiCorruptionLayer {

    @Override
    public ServiceReport toServiceModel(Map<String, Object> legacySummary) {
        throw new UnsupportedOperationException("TODO: translate the legacy summary into a ServiceReport");
    }
}
