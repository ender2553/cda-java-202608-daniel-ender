"""
scanner.py -- WS-VSW mock vulnerability scanner (STUDENT STARTER)

Reads an asset inventory + service inventory for a track, matches each
running service against a signature ("plugin") feed, and emits findings
in a Nessus-style structure to scan_results.json.

This is a MOCK tool: it never makes a network connection. All "scanning"
is local file matching against data/signatures.json.

YOUR TASK: implement the four functions marked TODO below. Everything
else (file loading, CLI, output) is already wired up. Run
    pytest test_scanner.py -v
as you go -- all tests should pass before you run the scanner for real.
"""
import argparse
import json
import os

from banner import print_banner

DATA_DIR = os.path.join(os.path.dirname(__file__), "..", "data")


def load_json(path):
    """Load and return the JSON contents of the given file path."""
    with open(path) as f:
        return json.load(f)


def parse_version(version_str):
    """
    Convert a dotted version string like '1.18.0' or '7.9p1' into a tuple
    of integers for comparison, e.g. (1, 18, 0) or (7, 9).

    Non-numeric trailing characters (like the 'p1' in '7.9p1') should be
    stripped from that segment.

    PREDICT FIRST: before writing this, predict what
    parse_version("7.9p1") should return, and what parse_version("10.2")
    should return. Write your predictions in your packet, then implement
    and check.

    TODO: implement this function.
    """
    # HINT: version_str.split(".") gives you the dot-separated segments.
    # For each segment, keep only the leading digit characters, then
    # convert what you kept to an int.
    raise NotImplementedError("TODO: implement parse_version")


def version_is_below(installed_version, threshold_version):
    """
    Return True if installed_version is strictly older than
    threshold_version, using parse_version() for comparison.

    TODO: implement this function using parse_version().
    """
    raise NotImplementedError("TODO: implement version_is_below; Hint: the function parse_version() - you have already written above, and you can call it here.")


def match_signatures(services_by_asset, signatures):
    """
    For every asset's running services, check each signature in the feed.
    Return a list of (asset_id, service, signature) tuples for every match
    where the service name matches signature['service_match'] AND the
    installed version is below signature['vulnerable_below'].

    TODO: implement this function.
    """
    # HINT: you'll need nested loops -- for each asset_id/services pair in
    # services_by_asset.items(), for each service in services, for each
    # sig in signatures, check the two conditions above.
    raise NotImplementedError("TODO: implement match_signatures")


def build_finding(asset, service, signature):
    """
    Build a single Nessus-style finding dict from an asset record, the
    vulnerable service, and the matched signature. The returned dict must
    have these keys: plugin_id, plugin_name, severity, host, asset_id, ip,
    port, protocol, service, installed_version, cve, cvss_vector,
    cvss_base_score, description, solution.

    TODO: implement this function.
    """
    raise NotImplementedError("TODO: implement build_finding")


SEVERITY_ORDER = {"critical": 0, "high": 1, "medium": 2, "low": 3, "info": 4}


def run_scan(track_dir, signatures_path):
    """
    Load assets/services for the given track directory and the shared
    signature feed, match them, and return a list of findings sorted by
    severity (critical first) and then by CVSS base score (highest first).
    """
    assets = load_json(os.path.join(track_dir, "assets.json"))
    services_by_asset = load_json(os.path.join(track_dir, "services.json"))
    signatures = load_json(signatures_path)

    assets_by_id = {a["asset_id"]: a for a in assets}

    findings = []
    for asset_id, service, sig in match_signatures(services_by_asset, signatures):
        asset = assets_by_id[asset_id]
        findings.append(build_finding(asset, service, sig))

    findings.sort(key=lambda f: (SEVERITY_ORDER.get(f["severity"], 9), -f["cvss_base_score"]))
    return findings


def main():
    parser = argparse.ArgumentParser(description="WS-VSW mock vulnerability scanner")
    parser.add_argument("--track", required=True,
                         choices=["track_a_meridian_health", "track_b_northbridge_financial",
                                  "track_c_coastal_logistics"])
    parser.add_argument("--out", default=None, help="Output path for scan_results.json")
    args = parser.parse_args()

    print_banner("Mock Vulnerability Scanner", [f"Track: {args.track}"])

    track_dir = os.path.join(DATA_DIR, args.track)
    signatures_path = os.path.join(DATA_DIR, "signatures.json")
    out_path = args.out or os.path.join(track_dir, "scan_results.json")

    findings = run_scan(track_dir, signatures_path)

    with open(out_path, "w") as f:
        json.dump(findings, f, indent=2)

    print(f"Scan complete for {args.track}: {len(findings)} findings written to {out_path}")
    for finding in findings:
        print(f"  [{finding['severity'].upper():8}] {finding['host']} - {finding['plugin_name']} ({finding['cve']})")


if __name__ == "__main__":
    main()
