package com.example.paintcalc.repository;

import com.example.paintcalc.domain.PaintEstimate;
import com.example.paintcalc.domain.Room;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Profile("memory")
public class InMemoryEstimateRepository implements EstimateRepository {
    private final Map<Long, List<PaintEstimate>> estimates = new HashMap<>();

    @Override
    public synchronized void save(PaintEstimate estimate, Room room) {
        estimates.computeIfAbsent(estimate.userId(), ignored -> new ArrayList<>()).add(estimate);
    }

    @Override
    public synchronized List<PaintEstimate> findByUserId(long userId) {
        return List.copyOf(estimates.getOrDefault(userId, List.of()));
    }
}
