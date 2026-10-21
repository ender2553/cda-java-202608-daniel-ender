package com.vitalledger.dto;

/**
 * Result of the LLM summarizer. {@code phiRedactedBeforeModel} is always true —
 * it documents that direct identifiers were stripped before the model call.
 */
public record SummaryResponse(
        Long recordId,
        String summary,
        boolean phiRedactedBeforeModel
) {
}
