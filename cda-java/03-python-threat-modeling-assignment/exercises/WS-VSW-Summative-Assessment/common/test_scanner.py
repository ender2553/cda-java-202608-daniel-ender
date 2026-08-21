"""
test_scanner.py -- run with: pytest test_scanner.py -v

These tests use small, self-contained fixtures (not the real track data)
so they give you the same pass/fail result no matter which track you're
on. Get all of these green before you run the scanner against your
track's real data.
"""
import scanner


def test_parse_version_simple():
    assert scanner.parse_version("1.18.0") == (1, 18, 0)


def test_parse_version_with_letters():
    # '7.9p1' -> the 'p1' suffix should not crash parsing; the '9' before
    # the letter still counts.
    assert scanner.parse_version("7.9p1") == (7, 9)


def test_version_is_below_true():
    assert scanner.version_is_below("1.18.0", "1.21.0") is True


def test_version_is_below_false_when_equal():
    assert scanner.version_is_below("1.21.0", "1.21.0") is False


def test_version_is_below_false_when_newer():
    assert scanner.version_is_below("1.22.0", "1.21.0") is False


def test_match_signatures_finds_vulnerable_service():
    services_by_asset = {
        "TEST-01": [{"name": "nginx", "version": "1.16.1", "port": 443, "protocol": "tcp"}]
    }
    signatures = [
        {"plugin_id": "WS-99999", "plugin_name": "Test Signature",
         "service_match": "nginx", "vulnerable_below": "1.21.0",
         "cve": "CVE-0000-0000", "cvss_vector": "AV:N", "cvss_base_score": 7.5,
         "severity": "high", "description": "test", "solution": "test"}
    ]
    matches = scanner.match_signatures(services_by_asset, signatures)
    assert len(matches) == 1
    asset_id, service, sig = matches[0]
    assert asset_id == "TEST-01"
    assert sig["plugin_id"] == "WS-99999"


def test_match_signatures_ignores_patched_service():
    services_by_asset = {
        "TEST-01": [{"name": "nginx", "version": "1.25.0", "port": 443, "protocol": "tcp"}]
    }
    signatures = [
        {"plugin_id": "WS-99999", "plugin_name": "Test Signature",
         "service_match": "nginx", "vulnerable_below": "1.21.0",
         "cve": "CVE-0000-0000", "cvss_vector": "AV:N", "cvss_base_score": 7.5,
         "severity": "high", "description": "test", "solution": "test"}
    ]
    matches = scanner.match_signatures(services_by_asset, signatures)
    assert matches == []


def test_match_signatures_ignores_unrelated_service():
    services_by_asset = {
        "TEST-01": [{"name": "postgresql", "version": "9.0.0", "port": 5432, "protocol": "tcp"}]
    }
    signatures = [
        {"plugin_id": "WS-99999", "plugin_name": "Test Signature",
         "service_match": "nginx", "vulnerable_below": "1.21.0",
         "cve": "CVE-0000-0000", "cvss_vector": "AV:N", "cvss_base_score": 7.5,
         "severity": "high", "description": "test", "solution": "test"}
    ]
    matches = scanner.match_signatures(services_by_asset, signatures)
    assert matches == []


def test_build_finding_shape():
    asset = {"asset_id": "TEST-01", "hostname": "test-host", "ip": "10.0.0.1"}
    service = {"name": "nginx", "version": "1.16.1", "port": 443, "protocol": "tcp"}
    signature = {"plugin_id": "WS-99999", "plugin_name": "Test Signature",
                 "cve": "CVE-0000-0000", "cvss_vector": "AV:N", "cvss_base_score": 7.5,
                 "severity": "high", "description": "test desc", "solution": "test fix"}
    finding = scanner.build_finding(asset, service, signature)
    assert finding["host"] == "test-host"
    assert finding["asset_id"] == "TEST-01"
    assert finding["port"] == 443
    assert finding["cve"] == "CVE-0000-0000"
    assert finding["cvss_base_score"] == 7.5
    assert finding["severity"] == "high"
