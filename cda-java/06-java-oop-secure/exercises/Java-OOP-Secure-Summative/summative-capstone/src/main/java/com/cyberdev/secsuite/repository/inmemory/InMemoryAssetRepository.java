package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.repository.AssetRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * In-memory stand-in so the whole application runs with zero database setup. Emulates the
 * PRIMARY KEY / UNIQUE constraints of the real table (a collision throws DataAccessException,
 * just as a real INSERT would), but deliberately does NOT emulate FOREIGN KEY constraints --
 * which is one reason services must check references at the application layer themselves
 * (SEC-4, SEC-10) instead of relying on "the database will catch it".
 *
 * searchByHostname here is a plain String.contains -- an in-memory map has no query language,
 * so there is nothing to inject into. SEC-15's injection lesson lives in JdbcAssetRepository.
 */
@Repository
@Profile("inmemory")
public class InMemoryAssetRepository implements AssetRepository {

    private final Map<Long, Asset> byId = new LinkedHashMap<>();

    @Override
    public synchronized Asset save(Asset asset) {
        if (asset == null) {
            throw new ValidationException("asset must not be null");
        }
        Asset persisted = asset.getId() <= 0 ? asset.withId(InMemoryIds.nextId(byId)) : asset;
        if (byId.containsKey(persisted.getId())) {
            throw new DataAccessException("Duplicate asset id " + persisted.getId(), null);
        }
        if (findByHostname(persisted.getHostname()).isPresent()) {
            throw new DataAccessException("Duplicate asset hostname " + persisted.getHostname(), null);
        }
        byId.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public synchronized Optional<Asset> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized Optional<Asset> findByHostname(String hostname) {
        if (hostname == null) {
            return Optional.empty();
        }
        return byId.values().stream().filter(a -> a.getHostname().equals(hostname)).findFirst();
    }

    @Override
    public synchronized List<Asset> findAll() {
        return byId.values().stream().sorted(Comparator.comparing(Asset::getHostname)).toList();
    }

    @Override
    public synchronized List<Asset> searchByHostname(String keyword) {
        if (keyword == null) {
            return List.of();
        }
        return byId.values().stream()
                .filter(a -> a.getHostname().contains(keyword))
                .sorted(Comparator.comparing(Asset::getHostname))
                .toList();
    }
}
