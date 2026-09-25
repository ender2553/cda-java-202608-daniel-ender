package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Component;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.model.StrideCategory;
import com.cyberdev.secsuite.model.ThreatModelEntry;
import com.cyberdev.secsuite.repository.inmemory.InMemorySeedLoader;
import com.cyberdev.secsuite.support.SeededSuite;
import com.cyberdev.secsuite.testkit.GradedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * SEC-7 (SbomService.findVulnerableComponents) and the service half of SEC-8
 * (ThreatModelingService.strideCoverage). The Jdbc/RowMapper half of SEC-8 is in
 * repository.jdbc.RepositoryTests. In-memory seed, no database. Requires SEC-1.
 */
public class SbomAndThreatModelTests {

    private static final Long LOGLITE = 1L;

    // ---------------------------------------------------------------
    // SEC-7: SBOM vulnerability matching
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-7", points = 2, description = "every component appears once, in repository order -- components with no CVEs have an EMPTY list")
    public void sbom_everyComponentAppearsIncludingCleanOnes() {
        SeededSuite suite = SeededSuite.seeded();
        List<ComponentVulnerabilities> result = suite.sbom().findVulnerableComponents();

        assertNotNull(result);
        List<Component> expectedOrder = suite.components.findAll();
        assertEquals(8, result.size(), "all 8 seeded components must be listed -- do not filter out clean ones");
        for (int i = 0; i < expectedOrder.size(); i++) {
            assertEquals(expectedOrder.get(i), result.get(i).component(),
                    "components must keep the repository order (application, name, version) at index " + i);
            assertNotNull(result.get(i).cves(), "the CVE list is never null");
        }
        List<String> clean = new ArrayList<>();
        for (ComponentVulnerabilities cv : result) {
            if (cv.cves().isEmpty()) {
                clean.add(cv.component().getComponentName());
            }
        }
        assertEquals(List.of("date-helper", "requests-lite", "org.example:templating-core"), clean,
                "exactly these three components have no known CVEs and must still appear");
        assertEquals(5L, result.stream().filter(ComponentVulnerabilities::isVulnerable).count());
    }

    @Test
    @GradedTest(tag = "SEC-7", points = 1, description = "each link resolves to the full catalog entry (many-to-many, several CVEs per component)")
    public void sbom_linksResolveToCatalogEntries() {
        SeededSuite suite = SeededSuite.seeded();
        List<ComponentVulnerabilities> result = suite.sbom().findVulnerableComponents();

        ComponentVulnerabilities widgets = result.stream()
                .filter(cv -> cv.component().getComponentName().equals("admin-ui-widgets"))
                .findFirst().orElseThrow();
        List<String> widgetCves = widgets.cves().stream().map(CveCatalogEntry::cveId).toList();
        assertEquals(List.of("CVE-2023-91005", "CVE-2024-91007"), widgetCves,
                "admin-ui-widgets is linked to TWO CVEs (sorted worst first by ComponentVulnerabilities)");

        ComponentVulnerabilities loglite = result.stream()
                .filter(cv -> cv.component().getId().equals(LOGLITE))
                .findFirst().orElseThrow();
        assertEquals(1, loglite.cves().size());
        assertEquals(new BigDecimal("10.0"), loglite.cves().get(0).cvssScore(),
                "the entry must be the catalog's own record (CVSS 10.0), looked up through the repository");
    }

    @Test
    @GradedTest(tag = "SEC-7", points = 1, description = "a link to a CVE missing from the catalog fails closed instead of being dropped")
    public void sbom_danglingLinkFailsClosed() {
        SeededSuite suite = SeededSuite.seeded();
        suite.components.linkCve(LOGLITE, "CVE-2030-12345"); // the in-memory repo has no foreign keys
        assertThrows(ValidationException.class, () -> suite.sbom().findVulnerableComponents(),
                "silently skipping the dangling link would under-report a vulnerability");
    }

    // ---------------------------------------------------------------
    // SEC-8 (service half): STRIDE coverage
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-8", points = 2, description = "strideCoverage returns all SIX categories in enum order, with empty lists for the gaps")
    public void strideCoverage_allSixCategoriesInOrder() {
        SeededSuite suite = SeededSuite.seeded();
        Map<StrideCategory, List<ThreatModelEntry>> coverage =
                suite.threatModeling().strideCoverage(InMemorySeedLoader.STOREFRONT_THREAT_MODEL);

        assertEquals(Arrays.asList(StrideCategory.values()), new ArrayList<>(coverage.keySet()),
                "all six STRIDE categories must be keys, in declaration order S, T, R, I, D, E");
        assertNotNull(coverage.get(StrideCategory.REPUDIATION), "a category with no entries maps to an EMPTY list");
        assertTrue(coverage.get(StrideCategory.REPUDIATION).isEmpty(), "storefront has no Repudiation threats");
        assertTrue(coverage.get(StrideCategory.ELEVATION_OF_PRIVILEGE).isEmpty(),
                "storefront has no Elevation of Privilege threats");
        assertEquals(1, coverage.get(StrideCategory.SPOOFING).size());
        assertEquals(1, coverage.get(StrideCategory.TAMPERING).size());
        assertEquals(2, coverage.get(StrideCategory.INFORMATION_DISCLOSURE).size());
        assertEquals(1, coverage.get(StrideCategory.DENIAL_OF_SERVICE).size());

        Map<StrideCategory, List<ThreatModelEntry>> vpn =
                suite.threatModeling().strideCoverage(InMemorySeedLoader.VPN_THREAT_MODEL);
        for (StrideCategory category : StrideCategory.values()) {
            assertEquals(1, vpn.get(category).size(), "the VPN model covers every category once: " + category);
            assertEquals(category, vpn.get(category).get(0).getStrideCategory(),
                    "an entry must be filed under its own category");
        }
    }

    @Test
    @GradedTest(tag = "SEC-8", points = 1, description = "coverage of an unknown or null threat model fails closed (not an empty map)")
    public void strideCoverage_unknownModelFailsClosed() {
        SeededSuite suite = SeededSuite.seeded();
        ThreatModelingService service = suite.threatModeling();
        assertThrows(ValidationException.class, () -> service.strideCoverage(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE)),
                "\"coverage of a model that doesn't exist\" must not look like \"a model with zero threats\"");
        assertThrows(ValidationException.class, () -> service.strideCoverage(null));
    }
}
