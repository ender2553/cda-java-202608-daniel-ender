package com.cyberdev.dndrpg.repository.jdbc;

import com.cyberdev.dndrpg.exception.DataAccessException;
import com.cyberdev.dndrpg.repository.LeaderboardRepository;
import com.cyberdev.dndrpg.repository.jdbc.mapper.LeaderboardEntryRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

/** INSTRUCTOR REFERENCE IMPLEMENTATION (DATA-9 solved). */
@Repository
@Profile("jdbc")
public final class JdbcLeaderboardRepository implements LeaderboardRepository {

    private final JdbcTemplate jdbcTemplate;
    private final LeaderboardEntryRowMapper rowMapper = new LeaderboardEntryRowMapper();

    public JdbcLeaderboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void recordScore(String characterName, int score) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO leaderboard_entry (character_name, score) VALUES (?, ?)",
                    characterName, score);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to record score for " + characterName, e);
        }
    }

    @Override
    public List<LeaderboardEntry> topScores(int limit) {
        try {
            return jdbcTemplate.query(
                    "SELECT character_name, score FROM leaderboard_entry ORDER BY score DESC LIMIT ?",
                    rowMapper, limit);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load leaderboard", e);
        }
    }
}
