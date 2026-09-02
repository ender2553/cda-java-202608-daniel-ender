package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A single third-party dependency entry in the Software Bill of Materials
 * (SBOM), as ingested from sbom_dependencies.json (untrusted input).
 *
 * USER STORY: As a security analyst relying on the SBOM risk heatmap, I
 * need each Dependency's list of known CVEs to be immutable after
 * construction, so a bug elsewhere in the app cannot silently make a
 * vulnerable dependency look "clean" by clearing its CVE list.
 *
 * ACCEPTANCE CRITERIA: identical pattern to ThreatIndicator --
 *  1. Mutating the List passed into the constructor after construction
 *     must not change this object's state.
 *  2. Mutating the List returned by getKnownCves() must not change this
 *     object's state (and should fail loudly, not silently).
 */
public final class Dependency {

    private final String name;
    private final String version;
    private final String ecosystem;       // allow-listed: maven, npm, pypi, nuget
    private final List<String> knownCves; // MUTABLE COLLECTION -- must be defended

    /**
     * TODO (ACCEPTANCE CRITERION 1): implement this constructor.
     *   - Assign name, version, and ecosystem directly, each guarded with
     *     Objects.requireNonNull(...).
     *   - For knownCves: copy the caller's list into a new private list
     *     (treat a null argument as an empty list), then wrap that copy
     *     as unmodifiable before assigning it to this.knownCves. See
     *     ThreatIndicator in this same package for the identical pattern
     *     applied to its "tags" field.
     */
    public Dependency(String name, String version, String ecosystem, List<String> knownCves) {
        throw new UnsupportedOperationException(
                "TODO: implement Dependency constructor with defensive copy of knownCves");
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getEcosystem() { return ecosystem; }

    /**
     * TODO (ACCEPTANCE CRITERION 2): implement this getter so that a
     * caller can never mutate this object's internal knownCves list
     * through the reference it returns.
     */
    public List<String> getKnownCves() {
        throw new UnsupportedOperationException("TODO: implement getKnownCves() defensive return");
    }

    public boolean isVulnerable() {
        return !knownCves.isEmpty();
    }

    @Override
    public String toString() {
        return "Dependency{" + name + "@" + version + " (" + ecosystem + "), cves=" + knownCves + "}";
    }
}
