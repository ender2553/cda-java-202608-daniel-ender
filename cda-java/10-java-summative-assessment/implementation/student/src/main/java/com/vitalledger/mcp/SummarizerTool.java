package com.vitalledger.mcp;

import com.vitalledger.dto.SummaryResponse;
import com.vitalledger.service.SummarizerService;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * MCP-style tool exposing the secure record summarizer. Advertised via
 * {@link McpToolRegistry} (tools/list) and invoked with a validated argument map
 * (tools/call). All PHI-minimization and prompt-injection defense live in
 * {@link SummarizerService}; this class is the thin, schema-bearing tool wrapper.
 */
@Component
public class SummarizerTool implements McpTool {

    private final SummarizerService summarizerService;

    public SummarizerTool(SummarizerService summarizerService) {
        this.summarizerService = summarizerService;
    }

    @Override
    public String name() {
        return "summarize_clinical_record";
    }

    @Override
    public String description() {
        return "Produce a PHI-minimized clinical summary of a single record by id. "
                + "Direct identifiers are redacted before the model call and record "
                + "content cannot alter the tool's instructions.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "recordId", Map.of(
                                "type", "integer",
                                "description", "The clinical record id to summarize")),
                "required", java.util.List.of("recordId"));
    }

    @Override
    public Object call(Map<String, Object> arguments) {
        // TODO(phase E): validate + coerce the required "recordId" argument to a long, then delegate to summarizerService.summarize(...).
        throw new UnsupportedOperationException("TODO(phase E)");
    }

    /** Typed convenience used by the controller. */
    public SummaryResponse summarize(Long recordId) {
        // TODO(phase E): invoke call(...) with the recordId and return the typed SummaryResponse.
        throw new UnsupportedOperationException("TODO(phase E)");
    }
}
