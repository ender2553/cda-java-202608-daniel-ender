package com.cyberdev.dndrpg.repository;

import java.util.List;

public interface LeaderboardRepository {
    void recordScore(String characterName, int score);
    List<LeaderboardEntry> topScores(int limit);

    /**
     * A plain immutable class, not a Java record: private final fields,
     * a validating-free simple constructor, and accessor methods,
     * written out by hand rather than generated.
     */
    class LeaderboardEntry {
        private final String characterName;
        private final int score;

        public LeaderboardEntry(String characterName, int score) {
            this.characterName = characterName;
            this.score = score;
        }

        public String characterName() {
            return characterName;
        }

        public int score() {
            return score;
        }
    }
}
