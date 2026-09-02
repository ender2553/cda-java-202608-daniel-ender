package org.example.model;

import java.util.Objects;

/**
 * Represents a single result from the Port/Service Scanner (Day 3 tool),
 * now hardened as an immutable value object.
 *
 * REFERENCE EXAMPLE: This class is fully implemented and provided to you
 * as a model of correct practice. Study it before implementing
 * {@link ThreatIndicator} and {@link Dependency}, which follow the same
 * pattern but contain a mutable field (a List) that YOU must defend.
 *
 * Why immutable? A PortScanResult represents a fact that was true at the
 * moment of scanning. Nothing in the application should ever be able to
 * "rewrite history" by mutating a scan result after the fact -- doing so
 * would let a bug (or an attacker who gained code-execution) quietly
 * falsify security telemetry that the risk register and executive report
 * depend on.
 */
public final class PortScanResult {

    private final String host;
    private final int port;
    private final String protocol;   // allow-listed: "TCP" or "UDP"
    private final String state;      // allow-listed: "OPEN", "CLOSED", "FILTERED"
    private final String bannerRaw;  // untrusted text banner grabbed from the service

    public PortScanResult(String host, int port, String protocol, String state, String bannerRaw) {
        // All fields are primitives or Strings (Strings are already immutable
        // in Java), so no defensive copy is needed here -- there is no
        // mutable object whose internal reference could be leaked.
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.state = Objects.requireNonNull(state, "state");
        this.bannerRaw = bannerRaw == null ? "" : bannerRaw;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getProtocol() { return protocol; }
    public String getState() { return state; }
    public String getBannerRaw() { return bannerRaw; }

    @Override
    public String toString() {
        return "PortScanResult{host='" + host + "', port=" + port +
                ", protocol='" + protocol + "', state='" + state + "'}";
    }
}
