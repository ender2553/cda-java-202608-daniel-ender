package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.Component;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO (the Jdbc implementation is SEC-6).
 *
 * Persistence for SBOM components (table "component") AND their CVE links (link table
 * "component_cve"). The link table has no identity of its own -- it only exists as part of a
 * component's story -- so it is owned by this repository rather than getting a repository of
 * its own. See AnalystRepository for the shared contract.
 */
public interface ComponentRepository {

    Component save(Component component);

    Optional<Component> findById(Long id);

    /** Every component, ordered by application_name, component_name, component_version. */
    List<Component> findAll();

    /** Records that {@code cveId} affects this component (one component_cve row). */
    void linkCve(Long componentId, String cveId);

    /** CVE ids linked to this component, ordered by cve_id; an EMPTY list (never null) if none. */
    List<String> findCveIdsByComponentId(Long componentId);
}
