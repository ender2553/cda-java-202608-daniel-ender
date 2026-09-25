package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Component;
import com.cyberdev.secsuite.repository.ComponentRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * In-memory stand-in so the whole application runs with zero database setup. Emulates the
 * PRIMARY KEY / UNIQUE constraints of the real table (a collision throws DataAccessException,
 * just as a real INSERT would), but deliberately does NOT emulate FOREIGN KEY constraints --
 * which is one reason services must check references at the application layer themselves
 * (SEC-4, SEC-10) instead of relying on "the database will catch it".
 *
 * The component_cve link table is modelled as a Map from component id to a sorted Set of CVE
 * ids -- a Set because (component_id, cve_id) is the link table's composite primary key.
 */
@Repository
@Profile("inmemory")
public class InMemoryComponentRepository implements ComponentRepository {

    private final Map<Long, Component> byId = new LinkedHashMap<>();
    private final Map<Long, Set<String>> cveLinks = new LinkedHashMap<>();

    @Override
    public synchronized Component save(Component component) {
        if (component == null) {
            throw new ValidationException("component must not be null");
        }
        Component persisted = component.getId() <= 0 ? component.withId(InMemoryIds.nextId(byId)) : component;
        if (byId.containsKey(persisted.getId())) {
            throw new DataAccessException("Duplicate component id " + persisted.getId(), null);
        }
        boolean naturalKeyTaken = byId.values().stream().anyMatch(c ->
                c.getApplicationName().equals(persisted.getApplicationName())
                        && c.getComponentName().equals(persisted.getComponentName())
                        && c.getComponentVersion().equals(persisted.getComponentVersion()));
        if (naturalKeyTaken) {
            throw new DataAccessException("Duplicate component " + persisted, null);
        }
        byId.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public synchronized Optional<Component> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized List<Component> findAll() {
        return byId.values().stream()
                .sorted(Comparator.comparing(Component::getApplicationName)
                        .thenComparing(Component::getComponentName)
                        .thenComparing(Component::getComponentVersion))
                .toList();
    }

    @Override
    public synchronized void linkCve(Long componentId, String cveId) {
        if (componentId == null || cveId == null) {
            throw new ValidationException("componentId and cveId must not be null");
        }
        Set<String> links = cveLinks.computeIfAbsent(componentId, id -> new TreeSet<>());
        if (!links.add(cveId)) {
            throw new DataAccessException("Duplicate component_cve link " + componentId + " -> " + cveId, null);
        }
    }

    @Override
    public synchronized List<String> findCveIdsByComponentId(Long componentId) {
        Set<String> links = componentId == null ? null : cveLinks.get(componentId);
        return links == null ? List.of() : List.copyOf(links);
    }
}
