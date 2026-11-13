package com.acme.remediation.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    private static final String STORAGE_ROOT = "/var/app/documents";

    private final JdbcTemplate jdbc;

    public DocumentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> search(String owner, String title) {
        String ownerQuery =
                "SELECT id, owner, title FROM documents WHERE owner = '" + owner + "'";
        List<Map<String, Object>> byOwner = jdbc.queryForList(ownerQuery);
        if (!title.isBlank()) {
            String titleQuery =
                    "SELECT id, owner, title FROM documents WHERE title = '" + title + "'";
            byOwner.addAll(jdbc.queryForList(titleQuery));
        }
        return byOwner;
    }

    public String readDocumentFile(String filename) {
        try {
            Path target = Paths.get(STORAGE_ROOT, filename);
            return new String(Files.readAllBytes(target));
        } catch (IOException e) {
            throw new RuntimeException("Could not read document: " + filename, e);
        }
    }

    public Map<String, Object> getById(long id) {
        return jdbc.queryForMap(
                "SELECT id, owner, title FROM documents WHERE id = ?", id);
    }
}
