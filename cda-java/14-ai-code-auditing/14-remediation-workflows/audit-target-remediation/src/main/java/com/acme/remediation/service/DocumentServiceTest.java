package com.acme.remediation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        jdbc.execute("DROP TABLE IF EXISTS documents");
        jdbc.execute("""
                CREATE TABLE documents (
                    id BIGINT PRIMARY KEY,
                    owner VARCHAR(255) NOT NULL,
                    title VARCHAR(255) NOT NULL
                )
                """);

        jdbc.update(
                "INSERT INTO documents VALUES (?, ?, ?)",
                1L, "alice", "Q3 Plan");

        jdbc.update(
                "INSERT INTO documents VALUES (?, ?, ?)",
                2L, "bob", "Roadmap");

        documentService = new DocumentService(jdbc);
    }

    @Test
    void sqlInjectionMustNotReturnOtherOwnersDocuments() {

        String maliciousOwner = "x' OR '1'='1";

        List<Map<String, Object>> results =
                documentService.search(maliciousOwner, "");

        assertTrue(results.isEmpty(),
                "SQL injection must not return documents belonging to other owners");
    }

    @Test
    void legitimateOwnerSearchMustStillWork() {

        List<Map<String, Object>> results =
                documentService.search("alice", "");

        assertEquals(1, results.size());
        assertEquals("alice", results.get(0).get("OWNER"));
    }
}
