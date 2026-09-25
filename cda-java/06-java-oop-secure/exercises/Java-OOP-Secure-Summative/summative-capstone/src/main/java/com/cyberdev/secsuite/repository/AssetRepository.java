package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.Asset;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO (the Jdbc implementation is SEC-3/SEC-15).
 *
 * Persistence for the asset inventory (table "asset"). See AnalystRepository for the contract
 * shared by every repository in this package.
 */
public interface AssetRepository {

    Asset save(Asset asset);

    Optional<Asset> findById(Long id);

    Optional<Asset> findByHostname(String hostname);

    /** Every asset, ordered by hostname. */
    List<Asset> findAll();

    /**
     * Assets whose hostname CONTAINS the keyword (case-sensitive substring match), ordered by
     * hostname. The keyword is untrusted analyst input -- see SEC-15.
     */
    List<Asset> searchByHostname(String keyword);
}
