package com.acme.reporting;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportService {

    private static final Set<String> SORTABLE_COLUMNS =
            Set.of("created_at", "title", "owner", "status");

    private final JdbcTemplate jdbc;

    public ReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findByStatus(String status) {
        String sql = "SELECT id, title, owner, status FROM reports WHERE status = '"
                + status + "'";
        return jdbc.queryForList(sql);
    }

    public List<Map<String, Object>> rankedBy(String sort) {
        String column = sort == null ? "created_at" : sort.toLowerCase();
        if (!SORTABLE_COLUMNS.contains(column)) {
            column = "created_at";
        }
        String sql = "SELECT id, title, owner, status FROM reports ORDER BY " + column + " DESC";
        return jdbc.queryForList(sql);
    }
}
