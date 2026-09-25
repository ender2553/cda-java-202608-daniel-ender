package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.CveCatalogEntry;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Read-mostly access to the local CVE catalog (table "cve_catalog"). See AnalystRepository for
 * the shared contract.
 */
public interface CveCatalogRepository {

    void save(CveCatalogEntry entry);

    Optional<CveCatalogEntry> findById(String cveId);

    /** Every catalog entry, ordered by cve_id. */
    List<CveCatalogEntry> findAll();
}
