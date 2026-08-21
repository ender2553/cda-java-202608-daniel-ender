"""
test_heatmap.py -- run with: pytest test_heatmap.py -v

These tests only check the data-bucketing logic (bucket_score, build_grid).
The chart image itself isn't auto-graded -- your instructor will look at
the generated PNG directly.
"""
import heatmap


def test_bucket_score_rounds_normally():
    assert heatmap.bucket_score(3.4) == 3
    assert heatmap.bucket_score(3.6) == 4
    assert heatmap.bucket_score(3.0) == 3


def test_bucket_score_clamps_low():
    assert heatmap.bucket_score(0.2) == 1


def test_bucket_score_clamps_high():
    assert heatmap.bucket_score(5.9) == 5


def test_build_grid_places_finding_in_correct_cell():
    register = [
        {"host": "test-host", "plugin_name": "Test Finding",
         "likelihood_score": 4.6, "impact_score": 2.1},
    ]
    grid = heatmap.build_grid(register)
    # likelihood 4.6 -> bucket 5 -> column index 4
    # impact 2.1 -> bucket 2 -> row index 1
    assert grid[1][4] == ["test-host: Test Finding"]


def test_build_grid_is_5x5():
    grid = heatmap.build_grid([])
    assert len(grid) == 5
    assert all(len(row) == 5 for row in grid)


def test_build_grid_empty_cells_are_empty_lists():
    grid = heatmap.build_grid([])
    assert grid[0][0] == []
    assert grid[4][4] == []


def test_build_grid_multiple_findings_same_cell():
    register = [
        {"host": "host-a", "plugin_name": "Finding A", "likelihood_score": 3.0, "impact_score": 3.0},
        {"host": "host-b", "plugin_name": "Finding B", "likelihood_score": 3.4, "impact_score": 2.6},
    ]
    grid = heatmap.build_grid(register)
    # both should bucket to likelihood 3 (col idx 2), impact 3 (row idx 2)
    assert len(grid[2][2]) == 2
