package com.cyberdev.dndrpg.repository.jdbc.mapper;

import com.cyberdev.dndrpg.repository.LeaderboardRepository.LeaderboardEntry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/** PROVIDED -- maps one row of leaderboard_entry to a LeaderboardEntry. */
public final class LeaderboardEntryRowMapper implements RowMapper<LeaderboardEntry> {
    @Override
    public LeaderboardEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LeaderboardEntry(rs.getString("character_name"), rs.getInt("score"));
    }
}
