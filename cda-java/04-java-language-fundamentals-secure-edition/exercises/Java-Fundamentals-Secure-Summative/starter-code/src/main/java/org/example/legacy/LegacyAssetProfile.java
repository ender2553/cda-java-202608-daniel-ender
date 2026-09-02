package org.example.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * STUDENT TODO VERSION.
 *
 * This class originally shipped with three bugs. Your job is to write a
 * correct, immutable implementation from scratch -- use the bug
 * descriptions below as a checklist of mistakes NOT to reproduce:
 *
 *   BUG 1: The constructor stored the caller's List<String> reference
 *          directly instead of copying it, so mutating the caller's list
 *          after construction silently changed this object's state.
 *   BUG 2: getOpenPorts() returned the live internal List<String>
 *          reference, so ANY caller could add/remove/clear ports on an
 *          asset profile that is supposed to represent a fixed snapshot
 *          -- e.g., a compromised or buggy component elsewhere in the
 *          app could hide an open port from the risk register simply by
 *          calling profile.getOpenPorts().clear().
 *   BUG 3: The class exposed a setHostname() setter for no functional
 *          reason, making the whole object needlessly mutable when
 *          nothing in the domain ever legitimately changes an asset's
 *          identity after discovery.
 *
 * REQUIRED FIX: make the class immutable end-to-end -- final fields, NO
 * setters (do not add a setHostname() method at all -- this is a
 * deletion, not something to reimplement), defensive copy in the
 * constructor, and an unmodifiable view returned from the getter.
 */
public final class LegacyAssetProfile {

    private final String assetId;
    private final String hostname;
    private final List<String> openPorts;

    /**
     * TODO (fixes BUG 1): assign assetId and hostname directly, each
     * guarded with Objects.requireNonNull(...). For openPorts, do NOT
     * store the caller's reference -- copy it into a new private list
     * (treat null as an empty list), then wrap that copy as unmodifiable
     * before assigning to this.openPorts.
     */
    public LegacyAssetProfile(String assetId, String hostname, List<String> openPorts) {
        this.assetId = Objects.requireNonNull(assetId);
        this.hostname = Objects.requireNonNull(hostname);

        List<String> copy = new ArrayList<>(
                openPorts == null ? Collections.emptyList() : openPorts
        );

        this.openPorts = Collections.unmodifiableList(copy);
    }

    public String getAssetId() { return assetId; }
    public String getHostname() { return hostname; }

    /**
     * TODO (fixes BUG 2): return the unmodifiable openPorts field
     * directly -- never a reference that could be used to reach into and
     * mutate this object's internal state.
     */
    public List<String> getOpenPorts() {
        return openPorts;
    }

    // Fix for BUG 3: do NOT add a setHostname() method here. An asset's
    // identity is fixed at discovery time.

    @Override
    public String toString() {
        return "LegacyAssetProfile{" + assetId + " (" + hostname + "), openPorts=" + openPorts + "}";
    }
}
