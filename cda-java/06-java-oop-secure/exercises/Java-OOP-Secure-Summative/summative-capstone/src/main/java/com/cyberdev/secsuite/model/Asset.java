package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A host in the organization's inventory (maps to the "asset" table). Scan findings, risk
 * register entries and threat models all hang off an asset.
 *
 * Immutable: every field is final and there are no setters. A change (e.g. re-assigning the
 * owner team) is a new Asset instance with the same id, which is exactly why equality below
 * is by id alone.
 */
public final class Asset {

    /**
     * Dotted-quad IPv4 with every octet constrained to 0-255 (so "999.1.1.1" is rejected, not
     * just "abc"). IPv6 is intentionally out of scope for this capstone.
     */
    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$");

    /**
     * RFC 1123-style hostname: dot-separated labels of letters, digits and inner hyphens, each
     * label 1-63 chars. Anything else (spaces, quotes, semicolons, slashes...) is rejected.
     */
    private static final Pattern HOSTNAME = Pattern.compile(
            "^[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$");

    private static final int MAX_HOSTNAME_LENGTH = 253;

    private final Long id;
    private final String hostname;
    private final String ipAddress;
    private final String ownerTeam;
    private final Criticality criticality;

    // INSTRUCTOR NOTE [SEC-1]: Concept tested: fail-closed constructor validation -- the
    // constructor is the one place every Asset must pass through, so validating HERE means an
    // invalid Asset can never exist anywhere in the program (the same "validate at
    // construction" lesson as QuickPay's Money compact constructor). Rules: id non-null (0 is
    // the transient sentinel replaced by the repository with a generated identity);
    // hostname non-blank, at most 253 chars, RFC 1123 shape; ipAddress non-blank and
    // IPv4-shaped with every octet 0-255; ownerTeam non-blank; criticality non-null. Every
    // failure throws ValidationException (never IllegalArgumentException, never a silent
    // default like criticality = LOW). Values are trimmed BEFORE validation and the trimmed
    // value is what gets stored. Common mistakes: (1) validating a trimmed local variable but
    // assigning the untrimmed parameter to the field; (2) an IPv4 regex like "\\d+\\.\\d+\\.\\d+\\.\\d+"
    // that happily accepts "999.999.999.999"; (3) using String.matches without anchors in a
    // hand-written loop, or Matcher.find() instead of matches(), so "x10.0.0.1y" passes;
    // (4) defaulting a null criticality to LOW "to be helpful" -- an asset with unknown
    // criticality must be REJECTED, because LOW silently deprioritizes every finding on it.
    // SECURITY CALLOUT: allow-list validation (describe what IS valid, reject everything else)
    // is the correct posture for untrusted input. A hostname like "web01'; DROP TABLE asset;--"
    // is rejected here by shape alone -- but note this is defense in depth, NOT the SQL
    // injection fix: parameterized queries (SEC-3/SEC-15) are still mandatory, because not
    // every value that reaches SQL passes through a validating constructor first (e.g. a search
    // keyword never does).
    public Asset(Long id, String hostname, String ipAddress, String ownerTeam, Criticality criticality) {
        if (id == null) {
            throw new ValidationException("asset id must not be null");
        }
        throw new UnsupportedOperationException(
                "TODO [SEC-1]: validate and trim every field (fail closed with ValidationException), then assign the trimmed values");
    }

    /** GIVEN helper -- same IPv4 rule as the constructor, reused by the CSV ingestion validator. */
    public static boolean isValidIpv4(String candidate) {
        return candidate != null && IPV4.matcher(candidate).matches();
    }

    public Long getId() {
        return id;
    }

    public Asset withId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive");
        }
        return new Asset(id, hostname, ipAddress, ownerTeam, criticality);
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getOwnerTeam() {
        return ownerTeam;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    // INSTRUCTOR NOTE [SEC-1]: Asset is an ENTITY -- equal by IDENTITY (id) alone, exactly
    // like QuickPay's Merchant. Two Asset objects with the same id but a different ownerTeam
    // are the SAME host observed at two points in time (before/after a re-org), not two
    // different hosts; full-field equality would make a Set<Asset> think a re-org added a
    // server. Contrast with CveCatalogEntry, a record with VALUE equality. Distinct transient
    // Assets both use id 0L but are NOT equal; generated identity starts only after
    // persistence. equals() and hashCode() must always be overridden together.
    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException(
                "TODO [SEC-1]: persisted Assets are equal by positive id; distinct transient id-0 Assets are not equal");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
                "TODO [SEC-1]: hashCode consistent with equals (id only)");
    }

    @Override
    public String toString() {
        return "Asset{" + hostname + " " + ipAddress + " " + criticality + ", owner=" + ownerTeam + "}";
    }
}
