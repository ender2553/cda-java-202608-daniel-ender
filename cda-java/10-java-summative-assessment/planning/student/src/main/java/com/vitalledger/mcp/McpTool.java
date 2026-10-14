package com.vitalledger.mcp;

import java.util.Map;

/**
 * A minimal MCP-style tool contract: a named, described, schema-bearing callable.
 * This mirrors how a Model Context Protocol server advertises tools (name +
 * description + input schema) and services {@code tools/call} invocations.
 */
public interface McpTool {

    String name();

    String description();

    /** JSON-schema-shaped description of the tool's input arguments. */
    Map<String, Object> inputSchema();

    /** Invoke the tool with a validated argument map. */
    Object call(Map<String, Object> arguments);
}
