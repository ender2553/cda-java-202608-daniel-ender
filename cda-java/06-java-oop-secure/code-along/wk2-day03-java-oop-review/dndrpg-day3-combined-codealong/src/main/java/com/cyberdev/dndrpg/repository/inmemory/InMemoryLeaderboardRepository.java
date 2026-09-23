package com.cyberdev.dndrpg.repository.inmemory;

import com.cyberdev.dndrpg.repository.LeaderboardRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
@Profile("!jdbc")
public final class InMemoryLeaderboardRepository implements LeaderboardRepository {
    private final List<LeaderboardEntry> entries = new CopyOnWriteArrayList<>();

    @Override
    public void recordScore(String characterName, int score) {
        entries.add(new LeaderboardEntry(characterName, score));
    }

    @Override
    public List<LeaderboardEntry> topScores(int limit) {
        List<LeaderboardEntry> sorted = new ArrayList<>(entries);
        sorted.sort((a, b) -> Integer.compare(b.score(), a.score()));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }
}
