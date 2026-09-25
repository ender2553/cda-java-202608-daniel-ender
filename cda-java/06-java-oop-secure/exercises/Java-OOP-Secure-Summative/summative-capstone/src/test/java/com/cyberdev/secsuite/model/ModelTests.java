package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.testkit.GradedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * SEC-1 (Asset validation + entity equality) and SEC-2 (CVSS severity classification). No
 * database, no seed data: these are the two checkpoints everything else builds on, so they
 * are tested in isolation.
 */
public class ModelTests {

    private static final Long ID = 170L;

    // ---------------------------------------------------------------
    // SEC-1: Asset constructor validation and entity equality
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-1", points = 1, description = "valid input is trimmed before validation and the TRIMMED values are stored")
    public void asset_validInputIsTrimmedAndStored() {
        Asset asset = new Asset(ID, "  web-prod-01 ", " 10.0.1.10 ", "  Platform Engineering ", Criticality.CRITICAL);
        assertEquals(ID, asset.getId());
        assertEquals("web-prod-01", asset.getHostname(), "hostname must be stored trimmed");
        assertEquals("10.0.1.10", asset.getIpAddress(), "ipAddress must be stored trimmed");
        assertEquals("Platform Engineering", asset.getOwnerTeam(), "ownerTeam must be stored trimmed");
        assertEquals(Criticality.CRITICAL, asset.getCriticality());
        assertDoesNotThrow(() -> new Asset(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE), "db.internal.example", "0.0.0.0", "Ops", Criticality.LOW),
                "a dotted hostname and the 0.0.0.0 address are valid");
        assertDoesNotThrow(() -> new Asset(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE), "h", "255.255.255.255", "Ops", Criticality.LOW),
                "a one-character hostname and 255.255.255.255 are valid");
    }

    @Test
    @GradedTest(tag = "SEC-1", points = 1, description = "every invalid field fails closed with ValidationException (no defaults, no IllegalArgumentException)")
    public void asset_rejectsInvalidFieldsFailClosed() {
        assertThrows(ValidationException.class, () -> new Asset(null, "web01", "10.0.0.1", "Ops", Criticality.LOW),
                "null id must be rejected");
        assertThrows(ValidationException.class, () -> new Asset(ID, null, "10.0.0.1", "Ops", Criticality.LOW),
                "null hostname must be rejected");
        assertThrows(ValidationException.class, () -> new Asset(ID, "   ", "10.0.0.1", "Ops", Criticality.LOW),
                "blank hostname must be rejected");
        assertThrows(ValidationException.class,
                () -> new Asset(ID, "web01'; DROP TABLE asset;--", "10.0.0.1", "Ops", Criticality.LOW),
                "a hostname containing quotes/semicolons/spaces is not RFC 1123-shaped and must be rejected");
        assertThrows(ValidationException.class, () -> new Asset(ID, "-web01", "10.0.0.1", "Ops", Criticality.LOW),
                "a label may not start with a hyphen");
        assertThrows(ValidationException.class,
                () -> new Asset(ID, "a".repeat(64) + ".example", "10.0.0.1", "Ops", Criticality.LOW),
                "a 64-character label is too long (max 63)");
        String longHost = ("a".repeat(63) + ".").repeat(4) + "com"; // 259 characters
        assertThrows(ValidationException.class, () -> new Asset(ID, longHost, "10.0.0.1", "Ops", Criticality.LOW),
                "a hostname longer than 253 characters must be rejected");
        assertThrows(ValidationException.class, () -> new Asset(ID, "web01", "999.1.1.1", "Ops", Criticality.LOW),
                "an octet above 255 must be rejected (a \\d+ regex is not enough)");
        assertThrows(ValidationException.class, () -> new Asset(ID, "web01", "10.0.0", "Ops", Criticality.LOW),
                "three octets is not an IPv4 address");
        assertThrows(ValidationException.class, () -> new Asset(ID, "web01", "x10.0.0.1y", "Ops", Criticality.LOW),
                "the IPv4 check must match the WHOLE value (anchored / matches(), not find())");
        assertThrows(ValidationException.class, () -> new Asset(ID, "web01", "  ", "Ops", Criticality.LOW),
                "blank ipAddress must be rejected");
        assertThrows(ValidationException.class, () -> new Asset(ID, "web01", "10.0.0.1", " ", Criticality.LOW),
                "blank ownerTeam must be rejected");
        assertThrows(ValidationException.class, () -> new Asset(ID, "web01", "10.0.0.1", "Ops", null),
                "null criticality must be REJECTED, never defaulted to LOW");
    }

    @Test
    @GradedTest(tag = "SEC-1", points = 1, description = "Asset is an entity: equals/hashCode by id only")
    public void asset_equalityIsByIdOnly() {
        Asset before = new Asset(ID, "web-prod-01", "10.0.1.10", "Platform Engineering", Criticality.CRITICAL);
        Asset afterReorg = new Asset(ID, "web-prod-01", "10.0.1.10", "Site Reliability", Criticality.HIGH);
        Asset twin = new Asset(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE), "web-prod-01", "10.0.1.10", "Platform Engineering",
                Criticality.CRITICAL);

        assertEquals(before, afterReorg, "same id = same host, even after its owner/criticality changed");
        assertEquals(before.hashCode(), afterReorg.hashCode(), "equal objects must have equal hash codes");
        assertNotEquals(before, twin, "a different id is a different host, even with identical fields");
        assertNotEquals(before, null, "equals(null) must be false");
        assertNotEquals(before, "web-prod-01", "equals(other type) must be false");
        Asset transientA = new Asset(0L, "new-a", "10.0.0.10", "Ops", Criticality.LOW);
        Asset transientB = new Asset(0L, "new-b", "10.0.0.11", "Ops", Criticality.LOW);
        assertNotEquals(transientA, transientB,
                "id 0L is only a transient sentinel; two unsaved assets do not share identity");

        Set<Asset> inventory = new HashSet<>();
        inventory.add(before);
        inventory.add(afterReorg);
        inventory.add(twin);
        assertEquals(2, inventory.size(), "a re-org must not look like a new server in a Set<Asset>");
    }

    // ---------------------------------------------------------------
    // SEC-2: Severity.fromCvssScore -- CVSS v3 bands, fail closed
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-2", points = 2, description = "every CVSS v3 band boundary maps to the right severity (no gaps, no overlaps)")
    public void severity_bandBoundaries() {
        double[] scores = {0.0, 0.1, 3.9, 3.95, 4.0, 5.3, 6.9, 6.95, 7.0, 8.9, 8.95, 9.0, 10.0};
        Severity[] expected = {
                Severity.NONE, Severity.LOW, Severity.LOW, Severity.LOW, Severity.MEDIUM, Severity.MEDIUM,
                Severity.MEDIUM, Severity.MEDIUM, Severity.HIGH, Severity.HIGH, Severity.HIGH, Severity.CRITICAL,
                Severity.CRITICAL};
        for (int i = 0; i < scores.length; i++) {
            assertEquals(expected[i], Severity.fromCvssScore(scores[i]),
                    "CVSS " + scores[i] + " should be " + expected[i]
                            + " (0.0 NONE, 0.1-3.9 LOW, 4.0-6.9 MEDIUM, 7.0-8.9 HIGH, 9.0-10.0 CRITICAL; "
                            + "values between bands such as 3.95 belong to the LOWER band)");
        }
    }

    @Test
    @GradedTest(tag = "SEC-2", points = 1, description = "scores outside 0.0-10.0, NaN and infinities are rejected, never bucketed")
    public void severity_rejectsOutOfDomain() {
        double[] invalid = {-0.1, -1.0, 10.1, 11.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double score : invalid) {
            assertThrows(ValidationException.class, () -> Severity.fromCvssScore(score),
                    "CVSS " + score + " is outside the CVSS domain and must throw ValidationException");
        }
    }
}
