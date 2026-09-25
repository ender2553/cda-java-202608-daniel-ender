package com.cyberdev.secsuite.repository.jdbc.mapper;

import com.cyberdev.secsuite.model.Component;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps one "component" row to a Component (SEC-6).
 *
 * Columns: component (id, application_name, component_name, component_version, ecosystem).
 */
public final class ComponentRowMapper implements RowMapper<Component> {

    // INSTRUCTOR NOTE [SEC-6]: Same RowMapper contract as SEC-3's AssetRowMapper: one row in,
    // one Component out, columns read by name, Long via rs.getObject("id", Long.class), no
    // rs.next(), construction through the validating constructor. Deliberately NOT here: the
    // component's CVEs. They live in the component_cve link table and are fetched separately
    // (ComponentRepository.findCveIdsByComponentId) -- a mapper that tried to JOIN them in
    // would return one row PER CVE, i.e. duplicate Components, and a component with no CVEs
    // would vanish from an INNER JOIN entirely (the exact failure SEC-7 guards against).
    @Override
    public Component mapRow(ResultSet rs, int rowNum) throws SQLException {
        throw new UnsupportedOperationException(
                "TODO [SEC-6]: map the current row to a Component, reading every column by name (never call rs.next())");
    }
}
