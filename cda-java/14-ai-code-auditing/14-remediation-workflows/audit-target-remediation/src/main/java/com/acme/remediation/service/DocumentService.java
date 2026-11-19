package com.acme.remediation.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    private static final Path STORAGE_ROOT =
            Paths.get("/var/app/documents");

    private final JdbcTemplate jdbc;
    private final Path storageRoot;

    public DocumentService(JdbcTemplate jdbc) {
        this(jdbc, STORAGE_ROOT);
    }

    // Constructor used by tests to provide a temporary storage directory.
    public DocumentService(JdbcTemplate jdbc, Path storageRoot) {
        this.jdbc = jdbc;
        this.storageRoot = storageRoot.toAbsolutePath().normalize();
    }

    // F-01: Use parameterized queries instead of SQL string concatenation.
    public List<Map<String, Object>> search(String owner, String title) {

        String ownerQuery =
                "SELECT id, owner, title FROM documents WHERE owner = ?";

        List<Map<String, Object>> results =
                jdbc.queryForList(ownerQuery, owner);

        if (title != null && !title.isBlank()) {

            String titleQuery =
                    "SELECT id, owner, title FROM documents WHERE title = ?";

            results.addAll(jdbc.queryForList(titleQuery, title));
        }

        return results;
    }

    // F-02: Prevent path traversal outside the storage directory.
    public String readDocumentFile(String filename) {

        try {
            Path requested = Paths.get(filename);

            // Reject absolute paths.
            if (requested.isAbsolute()) {
                throw new SecurityException("Invalid document path");
            }

            // Normalize the requested path.
            Path normalizedRoot = storageRoot.normalize();
            Path target = normalizedRoot.resolve(requested).normalize();

            // Ensure the normalized path stays inside the storage directory.
            if (!target.startsWith(normalizedRoot)) {
                throw new SecurityException("Invalid document path");
            }

            // Resolve actual filesystem paths to detect symbolic-link escapes.
            Path realRoot = normalizedRoot.toRealPath();
            Path realTarget = target.toRealPath();

            if (!realTarget.startsWith(realRoot)) {
                throw new SecurityException("Invalid document path");
            }

            // Only allow regular files.
            if (!Files.isRegularFile(realTarget)) {
                throw new SecurityException("Invalid document path");
            }

            return Files.readString(realTarget, StandardCharsets.UTF_8);

        } catch (SecurityException e) {
            throw e;

        } catch (IOException | InvalidPathException e) {
            throw new RuntimeException(
                    "Could not read requested document", e);
        }
    }

    public Map<String, Object> getById(long id) {

        return jdbc.queryForMap(
                "SELECT id, owner, title FROM documents WHERE id = ?", id);
    }
}