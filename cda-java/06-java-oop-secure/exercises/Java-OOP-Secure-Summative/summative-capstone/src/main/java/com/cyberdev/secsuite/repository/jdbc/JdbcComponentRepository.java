package com.cyberdev.secsuite.repository.jdbc;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Component;
import com.cyberdev.secsuite.repository.ComponentRepository;
import com.cyberdev.secsuite.repository.jdbc.mapper.ComponentRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

/**
 * ComponentRepository backed by the "component" and "component_cve" tables (SEC-6). See
 * JdbcAssetRepository's class note for why this is @Repository-annotated but constructed by
 * hand.
 */
@Repository
@Profile("jdbc")
public class JdbcComponentRepository implements ComponentRepository {

    private static final String SELECT_COLUMNS =
            "SELECT id, application_name, component_name, component_version, ecosystem FROM component";

    private final JdbcTemplate jdbcTemplate;
    private final ComponentRowMapper rowMapper = new ComponentRowMapper();

    public JdbcComponentRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    // INSTRUCTOR NOTE [SEC-6]: Concept tested: the SEC-3 pattern applied to a second
    // aggregate that owns a LINK TABLE. save() is the given generated-id example;
    // findById/findAll are exactly SEC-3's query shape
    // (parameterized INSERT/SELECT, empty-list -> Optional.empty(), wrap Spring's
    // DataAccessException). findAll must ORDER BY application_name, component_name,
    // component_version -- the SBOM section of the report is reproducible only if this order
    // is. linkCve inserts ONE component_cve row; findCveIdsByComponentId uses
    // jdbcTemplate.queryForList(sql, String.class, componentId) with ORDER BY cve_id and
    // returns an EMPTY list (queryForList never returns null) for a component with no CVEs.
    // Common mistakes: fetching components with an INNER JOIN to component_cve (components
    // with zero CVEs disappear, and components with two CVEs appear twice); building the
    // IN-list or the link insert by string concatenation; returning null instead of an empty
    // list from findCveIdsByComponentId.
    @Override
    public Component save(Component component) {
        if (component == null) {
            throw new ValidationException("component must not be null");
        }
        try {
            Long id = jdbcTemplate.queryForObject(
                    "INSERT INTO component (application_name, component_name, component_version, ecosystem) "
                            + "VALUES (?, ?, ?, ?) RETURNING id",
                    Long.class, component.getApplicationName(), component.getComponentName(),
                    component.getComponentVersion(), component.getEcosystem());
            return component.withId(id);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save component " + component.coordinates(), e);
        }
    }

    @Override
    public Optional<Component> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            List<Component> results = jdbcTemplate.query(
                    SELECT_COLUMNS + " WHERE id = ?",
                    rowMapper,
                    id
            );

            if (results.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(results.get(0));

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException(
                    "Failed to find component by id " + id, e);
        }
    }

    @Override
    public List<Component> findAll() {
        try {
            return jdbcTemplate.query(
                    SELECT_COLUMNS
                            + " ORDER BY application_name, component_name, component_version",
                    rowMapper
            );

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException(
                    "Failed to find all components", e);
        }
    }

    @Override
    public void linkCve(Long componentId, String cveId) {
        if (componentId == null) {
            throw new ValidationException("componentId must not be null");
        }

        if (cveId == null || cveId.isBlank()) {
            throw new ValidationException("cveId must not be blank");
        }

        try {
            jdbcTemplate.update(
                    "INSERT INTO component_cve (component_id, cve_id) VALUES (?, ?)",
                    componentId,
                    cveId
            );

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException(
                    "Failed to link CVE " + cveId
                            + " to component " + componentId,
                    e
            );
        }
    }

    @Override
    public List<String> findCveIdsByComponentId(Long componentId) {
        if (componentId == null) {
            return List.of();
        }

        try {
            return jdbcTemplate.queryForList(
                    "SELECT cve_id FROM component_cve "
                            + "WHERE component_id = ? "
                            + "ORDER BY cve_id",
                    String.class,
                    componentId
            );

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException(
                    "Failed to find CVEs for component " + componentId,
                    e
            );
        }
    }
}
