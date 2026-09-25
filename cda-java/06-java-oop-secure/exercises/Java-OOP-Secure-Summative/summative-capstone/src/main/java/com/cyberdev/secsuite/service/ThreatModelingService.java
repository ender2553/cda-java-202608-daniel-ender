package com.cyberdev.secsuite.service;

import org.springframework.stereotype.Service;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.StrideCategory;
import com.cyberdev.secsuite.model.ThreatModel;
import com.cyberdev.secsuite.model.ThreatModelEntry;
import com.cyberdev.secsuite.model.ThreatModelEntryStatus;
import com.cyberdev.secsuite.repository.AssetRepository;
import com.cyberdev.secsuite.repository.ThreatModelEntryRepository;
import com.cyberdev.secsuite.repository.ThreatModelRepository;

import java.time.Clock;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The STRIDE threat modeling tool: create threat models and entries, and report coverage.
 */
@Service
public class ThreatModelingService {

    private final ThreatModelRepository threatModelRepository;
    private final ThreatModelEntryRepository entryRepository;
    private final AssetRepository assetRepository;
    private final Clock clock;

    public ThreatModelingService(ThreatModelRepository threatModelRepository, ThreatModelEntryRepository entryRepository,
                                 AssetRepository assetRepository, Clock clock) {
        if (threatModelRepository == null || entryRepository == null || assetRepository == null || clock == null) {
            throw new ValidationException("ThreatModelingService dependencies must not be null");
        }
        this.threatModelRepository = threatModelRepository;
        this.entryRepository = entryRepository;
        this.assetRepository = assetRepository;
        this.clock = clock;
    }

    /** GIVEN -- not graded. Creates a threat model for an existing asset (unknown asset fails closed). */
    public ThreatModel createThreatModel(Long assetId, String title, String description) {
        if (assetId == null || assetRepository.findById(assetId).isEmpty()) {
            throw new ValidationException("Unknown asset id " + assetId + " -- threat model not created");
        }
        ThreatModel model = new ThreatModel(0L, assetId, title, description, clock.instant());
        return threatModelRepository.save(model);
    }

    /** GIVEN -- not graded. Adds one STRIDE entry to an existing threat model (unknown model fails closed). */
    public ThreatModelEntry addEntry(Long threatModelId, StrideCategory category, String description,
                                     String mitigation, ThreatModelEntryStatus status) {
        requireThreatModel(threatModelId);
        ThreatModelEntry entry = new ThreatModelEntry(0L, threatModelId, category, description,
                mitigation, status);
        return entryRepository.save(entry);
    }

    /** GIVEN -- every threat model, ordered by title. */
    public List<ThreatModel> listThreatModels() {
        return threatModelRepository.findAll();
    }

    // INSTRUCTOR NOTE [SEC-8]: Concept tested: exhaustive coverage over a closed set. The
    // returned map must contain ALL SIX StrideCategory keys, in the enum's declaration order
    // (S, T, R, I, D, E), EVEN WHEN a category has zero entries -- that category maps to an
    // empty list. The robust shape: (1) fail closed if the threat model id is null or unknown
    // (ValidationException -- "coverage of a model that doesn't exist" must not look like
    // "a model with zero threats"); (2) create an EnumMap and pre-populate it by looping over
    // StrideCategory.values(), so completeness comes from the ENUM, not from the data; (3) then
    // bucket each entry from entryRepository.findByThreatModelId into its category's list;
    // (4) return unmodifiable lists. Why it matters: in threat modeling, a missing category is
    // the finding. "We never considered Repudiation" is exactly what a reviewer needs to see,
    // and a map built only from the entries that exist (e.g. Collectors.groupingBy) silently
    // drops the six-minus-N categories nobody thought about. The seeded storefront model has
    // no REPUDIATION and no ELEVATION_OF_PRIVILEGE entries to catch this. Common mistakes:
    // Collectors.groupingBy(ThreatModelEntry::getStrideCategory) (no empty buckets); a HashMap
    // (random iteration order in the report); a switch with a `default` that lumps unknown
    // categories somewhere; depending on the repository returning entries in some order.
    // SECURITY CALLOUT: absence of evidence is not evidence of absence. Reports that only
    // render what exists make unexamined attack surface invisible.
    public Map<StrideCategory, List<ThreatModelEntry>> strideCoverage(Long threatModelId) {
        throw new UnsupportedOperationException(
                "TODO [SEC-8]: all six STRIDE categories in enum order, empty lists for gaps; unknown model fails closed");
    }

    private void requireThreatModel(Long threatModelId) {
        if (threatModelId == null || threatModelRepository.findById(threatModelId).isEmpty()) {
            throw new ValidationException("Unknown threat model id " + threatModelId);
        }
    }
}
