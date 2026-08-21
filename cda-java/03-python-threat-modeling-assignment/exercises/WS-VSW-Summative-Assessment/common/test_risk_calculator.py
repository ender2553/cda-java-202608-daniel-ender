"""
test_risk_calculator.py -- run with: pytest test_risk_calculator.py -v

Self-contained fixtures; same pass/fail result regardless of track.
"""
import risk_calculator as rc


def test_normalize_cvss_to_scale_boundaries():
    assert rc.normalize_cvss_to_scale(9.8) == 5
    assert rc.normalize_cvss_to_scale(8.0) == 5
    assert rc.normalize_cvss_to_scale(7.9) == 4
    assert rc.normalize_cvss_to_scale(6.0) == 4
    assert rc.normalize_cvss_to_scale(4.0) == 3
    assert rc.normalize_cvss_to_scale(2.0) == 2
    assert rc.normalize_cvss_to_scale(1.9) == 1
    assert rc.normalize_cvss_to_scale(0.0) == 1


def test_normalize_epss_to_scale_boundaries():
    assert rc.normalize_epss_to_scale(0.9) == 5
    assert rc.normalize_epss_to_scale(0.75) == 5
    assert rc.normalize_epss_to_scale(0.6) == 4
    assert rc.normalize_epss_to_scale(0.3) == 3
    assert rc.normalize_epss_to_scale(0.15) == 2
    assert rc.normalize_epss_to_scale(0.05) == 1


def test_compute_impact_weighted_average():
    asset = {"asset_criticality": 5, "data_sensitivity": 5, "business_impact": 5}
    # All 5s regardless of weights should give 5.0
    assert rc.compute_impact(asset) == 5.0

    asset2 = {"asset_criticality": 1, "data_sensitivity": 1, "business_impact": 1}
    assert rc.compute_impact(asset2) == 1.0


def test_compute_impact_diverging_factors():
    # High criticality, low sensitivity: mission-critical scheduler style asset.
    asset = {"asset_criticality": 5, "data_sensitivity": 1, "business_impact": 4}
    impact = rc.compute_impact(asset)
    # Should land strictly between the all-1s and all-5s cases.
    assert 1.0 < impact < 5.0


def test_compute_likelihood_high_everything():
    finding = {"cvss_base_score": 9.8}
    likelihood = rc.compute_likelihood(finding, epss_score=0.9, exploit_availability=5)
    assert likelihood == 5.0


def test_compute_likelihood_low_everything():
    finding = {"cvss_base_score": 0.0}
    likelihood = rc.compute_likelihood(finding, epss_score=0.0, exploit_availability=1)
    assert likelihood == 1.0


def test_compute_risk_score_is_product():
    assert rc.compute_risk_score(5, 5) == 25.0
    assert rc.compute_risk_score(1, 1) == 1.0
    assert rc.compute_risk_score(2, 3) == 6.0


def test_curveball_intel_update_raises_likelihood():
    # This mirrors the Phase 3 curveball: a low-CVSS finding whose EPSS and
    # exploit availability spike should see its likelihood score rise.
    finding = {"cvss_base_score": 5.3}
    before = rc.compute_likelihood(finding, epss_score=0.03, exploit_availability=2)
    after = rc.compute_likelihood(finding, epss_score=0.71, exploit_availability=5)
    assert after > before
