package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Objects;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One third-party component (library/package) in an application's software bill of
 * materials (maps to "component"). Deliberately holds NO list of CVEs: which CVEs affect which
 * components is a many-to-many relationship stored in the separate component_cve link table
 * (a List&lt;String&gt; field here would be the object-model version of a repeating group).
 * SbomService (SEC-7) assembles component + CVEs at read time.
 *
 * ENTITY: equal by id. (application, name, version) is also unique -- enforced by a UNIQUE
 * constraint in the schema -- but the surrogate id is what other rows reference.
 */
public final class Component {

    private final Long id;
    private final String applicationName;
    private final String componentName;
    private final String componentVersion;
    private final String ecosystem;

    public Component(Long id, String applicationName, String componentName, String componentVersion,
                     String ecosystem) {
        if (id == null) {
            throw new ValidationException("component id must not be null");
        }
        if (applicationName == null || applicationName.isBlank()) {
            throw new ValidationException("applicationName must not be blank");
        }
        if (componentName == null || componentName.isBlank()) {
            throw new ValidationException("componentName must not be blank");
        }
        if (componentVersion == null || componentVersion.isBlank()) {
            throw new ValidationException("componentVersion must not be blank");
        }
        if (ecosystem == null || ecosystem.isBlank()) {
            throw new ValidationException("ecosystem must not be blank");
        }
        this.id = id;
        this.applicationName = applicationName.trim();
        this.componentName = componentName.trim();
        this.componentVersion = componentVersion.trim();
        this.ecosystem = ecosystem.trim();
    }

    public Long getId() {
        return id;
    }

    public Component withId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive");
        }
        return new Component(id, applicationName, componentName, componentVersion, ecosystem);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public String getEcosystem() {
        return ecosystem;
    }

    /** e.g. "org.fictional:loglite@2.14.0". */
    public String coordinates() {
        return componentName + "@" + componentVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Component other)) return false;
        return id != null && id > 0 && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Component{" + applicationName + ": " + coordinates() + " (" + ecosystem + ")}";
    }
}
