package com.vitalledger.mcp;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Registry of MCP-style tools — the analog of an MCP server's {@code tools/list}.
 * Tools self-register as Spring beans; the registry exposes their advertised
 * descriptors and routes {@code tools/call} by name.
 */
@Component
public class McpToolRegistry {

    private final Map<String, McpTool> tools;

    public McpToolRegistry(List<McpTool> discovered) {
        this.tools = discovered.stream()
                .collect(Collectors.toUnmodifiableMap(McpTool::name, t -> t));
    }

    /** tools/list — advertised descriptors, no implementations leaked. */
    public List<Map<String, Object>> list() {
        // TODO(phase E): return each registered tool's advertised descriptor (name/description/inputSchema) — no implementations leaked.
        throw new UnsupportedOperationException("TODO(phase E)");
    }

    public McpTool require(String name) {
        // TODO(phase E): look up the tool by name, throwing IllegalArgumentException with a non-revealing message when unknown.
        throw new UnsupportedOperationException("TODO(phase E)");
    }
}
