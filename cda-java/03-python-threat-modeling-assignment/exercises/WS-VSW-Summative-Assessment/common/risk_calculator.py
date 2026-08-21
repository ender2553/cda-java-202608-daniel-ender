"""
risk_calculator.py -- WS-VSW contextual risk calculator (STUDENT STARTER)

Takes scan_results.json (from scanner.py) plus asset context, EPSS scores,
and exploit-availability intel, and produces a ranked risk register.

Risk = Likelihood x Impact, each on a 1-5 scale, giving a final score on a
1-25 scale. CVSS/EPSS/exploit availability describe how LIKELY exploitation
is; asset criticality/data sensitivity/business impact describe how much
it would HURT.

YOUR TASK: implement the five functions marked TODO below.
Run `pytest test_risk_calculator.py -v` as you go.

The weights below are intentionally editable constants -- after your
functions pass their tests, your packet asks you to change a weight and
re-run to see how the ranking shifts. Don't change them until then.
"""
import json
import os

DATA_DIR = os.path.join(os.path.dirname(__file__), "..", "data")

# ---- Editable weights (each 0-1; likelihood weights should sum to 1.0,
# impact weights should sum to 1.0) ----------------------------------------
LIKELIHOOD_WEIGHTS = {
    "cvss": 0.4,
    "epss": 0.4,
    "exploit_availability": 0.2,
}
IMPACT_WEIGHTS = {
    "asset_criticality": 0.4,
    "data_sensitivity": 0.3,
    "business_impact": 0.3,
}

NETWORK_ZONE_SCORE = {
    "internet_facing": 5,
    "dmz": 3,
    "internal": 2,
    "isolated": 1,
}


def load_json(path):
    """Load and return the JSON contents of the given file path."""
    with open(path) as f:
        return json.load(f)


def normalize_cvss_to_scale(cvss_base_score):
    """
    Convert a 0.0-10.0 CVSS base score to a 1-5 scale:
      8.0-10.0 -> 5
      6.0-7.9  -> 4
      4.0-5.9  -> 3
      2.0-3.9  -> 2
      0.0-1.9  -> 1

    TODO: implement this function.
    """
    #raise NotImplementedError("TODO: implement normalize_cvss_to_scale")

    if cvss_base_score >= 8:
        return 5
    if cvss_base_score >= 6:
        return 4
    if cvss_base_score >= 4:
        return 3
    if cvss_base_score >= 2:
        return 2
    return 1


def normalize_epss_to_scale(epss_score):
    """
    Convert a 0.0-1.0 EPSS probability score to a 1-5 scale:
      0.75-1.0  -> 5
      0.5-0.749 -> 4
      0.25-0.49 -> 3
      0.1-0.249 -> 2
      0.0-0.099 -> 1

    TODO: implement this function.
    """
    #raise NotImplementedError("TODO: implement normalize_epss_to_scale")

    if epss_score >= 0.75:
        return 5
    if epss_score >= 0.5:
        return 4
    if epss_score >= 0.25:
        return 3
    if epss_score >= 0.1:
        return 2
    return 1

def compute_likelihood(finding, epss_score, exploit_availability):
    """
    Combine normalized CVSS, normalized EPSS, and exploit availability
    (already 1-5) into a single weighted likelihood score (1-5, can be
    fractional), using LIKELIHOOD_WEIGHTS.

    PREDICT FIRST: before implementing, predict which of the three inputs
    will move the score the most if it swings from 1 to 5, given the
    current weights. Check your prediction once this is working.

    TODO: implement this function.
    """
    #raise NotImplementedError("TODO: implement compute_likelihood")

    cvss_scaled = normalize_cvss_to_scale(finding["cvss_base_score"])
    epss_scaled = normalize_epss_to_scale(epss_score)
    return (
        cvss_scaled * LIKELIHOOD_WEIGHTS["cvss"]
        + epss_scaled * LIKELIHOOD_WEIGHTS["epss"]
        + exploit_availability * LIKELIHOOD_WEIGHTS["exploit_availability"]
    )

def compute_impact(asset):
    """
    Combine an asset's criticality, data sensitivity, and business impact
    (each already 1-5) into a single weighted impact score (1-5, can be
    fractional), using IMPACT_WEIGHTS.

    TODO: implement this function.
    """
    #raise NotImplementedError("TODO: implement compute_impact")

    return (
        asset["asset_criticality"] * IMPACT_WEIGHTS["asset_criticality"]
        + asset["data_sensitivity"] * IMPACT_WEIGHTS["data_sensitivity"]
        + asset["business_impact"] * IMPACT_WEIGHTS["business_impact"]
    )

def compute_risk_score(likelihood, impact):
    """
    Combine likelihood (1-5) and impact (1-5) into a final risk score on a
    1-25 scale. Round to 2 decimal places.

    TODO: implement this function.
    """
    #raise NotImplementedError("TODO: implement compute_risk_score; You got this one! It's just a simple multiplication and rounding.")

    return round(likelihood * impact, 2)
        

def build_risk_register(track_dir, epss_path, exploit_intel_path, intel_update_path=None):
    """
    Load scan_results.json + assets.json for the track, plus EPSS and
    exploit-availability data, and return a list of risk-scored findings
    sorted highest-risk first.

    If intel_update_path is given, apply the Phase-3 threat-intel update
    (overriding EPSS / exploit-availability values for the CVEs it covers)
    before scoring.
    """
    findings = load_json(os.path.join(track_dir, "scan_results.json"))
    assets = {a["asset_id"]: a for a in load_json(os.path.join(track_dir, "assets.json"))}
    epss_lookup = load_json(epss_path)
    exploit_intel = load_json(exploit_intel_path)

    if intel_update_path and os.path.exists(intel_update_path):
        update = load_json(intel_update_path)
        epss_lookup.update(update.get("updated_epss", {}))
        exploit_intel.update(update.get("updated_exploit_intel", {}))

    register = []
    for finding in findings:
        asset = assets[finding["asset_id"]]
        epss_score = epss_lookup.get(finding["cve"], {}).get("epss_score", 0.0)
        exploit_avail = exploit_intel.get(finding["cve"], 1)

        likelihood = compute_likelihood(finding, epss_score, exploit_avail)
        impact = compute_impact(asset)
        risk_score = compute_risk_score(likelihood, impact)

        register.append({
            **finding,
            "network_zone": asset["network_zone"],
            "epss_score": epss_score,
            "exploit_availability": exploit_avail,
            "likelihood_score": round(likelihood, 2),
            "impact_score": round(impact, 2),
            "risk_score": risk_score,
        })

    register.sort(key=lambda r: r["risk_score"], reverse=True)
    return register


def main():
    import argparse
    from banner import print_banner

    parser = argparse.ArgumentParser(description="WS-VSW contextual risk calculator")
    parser.add_argument("--track", required=True,
                         choices=["track_a_meridian_health", "track_b_northbridge_financial",
                                  "track_c_coastal_logistics"])
    parser.add_argument("--apply-intel-update", action="store_true",
                         help="Apply the Phase 3 threat intel update before scoring")
    parser.add_argument("--out", default=None)
    args = parser.parse_args()

    print_banner("Contextual Risk Calculator", [
        f"Track: {args.track}",
        "Mode: threat intel update APPLIED" if args.apply_intel_update else "Mode: baseline",
    ])

    track_dir = os.path.join(DATA_DIR, args.track)
    epss_path = os.path.join(DATA_DIR, "epss_lookup.json")
    exploit_intel_path = os.path.join(DATA_DIR, "exploit_intel.json")
    intel_update_path = os.path.join(DATA_DIR, "threat_intel_update_INSTRUCTOR_ONLY.json") \
        if args.apply_intel_update else None
    out_path = args.out or os.path.join(track_dir, "risk_register.json")

    register = build_risk_register(track_dir, epss_path, exploit_intel_path, intel_update_path)

    with open(out_path, "w") as f:
        json.dump(register, f, indent=2)

    print(f"Risk register for {args.track} ({'with' if args.apply_intel_update else 'without'} intel update):")
    for r in register:
        print(f"  [{r['risk_score']:5.2f}] {r['host']:28} {r['plugin_name']} ({r['cve']})")


if __name__ == "__main__":
    main()
