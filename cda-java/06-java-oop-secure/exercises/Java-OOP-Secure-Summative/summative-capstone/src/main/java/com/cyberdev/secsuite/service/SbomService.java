package com.cyberdev.secsuite.service;

import org.springframework.stereotype.Service;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Component;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.repository.ComponentRepository;
import com.cyberdev.secsuite.repository.CveCatalogRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * The SBOM (software bill of materials) vulnerability matcher: which third-party components in
 * which applications carry which known CVEs.
 */
@Service
public class SbomService {

    private final ComponentRepository componentRepository;
    private final CveCatalogRepository cveCatalogRepository;

    public SbomService(ComponentRepository componentRepository, CveCatalogRepository cveCatalogRepository) {
        if (componentRepository == null || cveCatalogRepository == null) {
            throw new ValidationException("SbomService dependencies must not be null");
        }
        this.componentRepository = componentRepository;
        this.cveCatalogRepository = cveCatalogRepository;
    }

    // INSTRUCTOR NOTE [SEC-7]: Concept tested: assembling a many-to-many relationship
    // (component <-> component_cve <-> cve_catalog) in the SERVICE layer through repository
    // calls only -- no SQL in the service -- and handling the "zero" case as a first-class
    // result. For every component from componentRepository.findAll() (already ordered by
    // application, name, version): get its linked CVE ids via findCveIdsByComponentId, resolve
    // each through cveCatalogRepository.findById, and return one ComponentVulnerabilities per
    // component. A component with NO links must still appear, with an EMPTY list -- never
    // null, never skipped, never an exception. A link that points at a CVE missing from the
    // catalog is a data-integrity failure and fails closed with ValidationException (the
    // InMemory* repositories do not enforce the foreign key; don't silently drop the link and
    // under-report a vulnerability). Common mistakes: (1) filtering the result down to only
    // vulnerable components -- the report must say "no known vulnerabilities" explicitly,
    // which is itself useful information for an auditor; (2) returning null for the CVE list
    // (ComponentVulnerabilities rejects null to catch exactly this); (3) copying CVE
    // description/score into the Component object -- the denormalization the schema avoids;
    // (4) writing a JOIN in the service with a JdbcTemplate -- the service must work
    // identically on the InMemory* path.
    // Note for discussion: this is an N+1 access pattern (one lookup per link). Fine for an
    // SBOM of this size; a production Jdbc implementation would add a repository method that
    // does the three-table JOIN in one query -- the service contract would not change.
    public List<ComponentVulnerabilities> findVulnerableComponents() {
        List<ComponentVulnerabilities> result = new ArrayList<>();

        for (Component component : componentRepository.findAll()) {
            List<CveCatalogEntry> cves = new ArrayList<>();

            for (String cveId : componentRepository.findCveIdsByComponentId(component.getId())) {
                CveCatalogEntry cve = cveCatalogRepository.findById(cveId)
                        .orElseThrow(() -> new ValidationException(
                                "Component " + component.getId()
                                        + " references unknown CVE " + cveId));

                cves.add(cve);
            }

            result.add(new ComponentVulnerabilities(component, cves));
        }

        return result;
    }
}
