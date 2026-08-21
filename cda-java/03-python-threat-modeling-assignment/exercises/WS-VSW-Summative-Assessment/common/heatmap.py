"""
heatmap.py -- WS-VSW risk heat map generator (STUDENT STARTER)

Reads a track's risk_register.json (produced by risk_calculator.py) and
plots a classic 5x5 Likelihood x Impact risk matrix heat map, with each
cell shaded by finding count. Saves a PNG -- this is the visual you'd
actually hand to leadership alongside your executive summary.

YOUR TASK: implement bucket_score() and build_grid() below. The plotting
code (plot_heatmap) is fully provided -- once your two functions are
correct, the chart just works. Run
    pytest test_heatmap.py -v
before generating a real heat map.
"""
import argparse
import json
import os

import matplotlib
matplotlib.use("Agg")  # no display in this environment; write straight to file
import matplotlib.pyplot as plt
import numpy as np

from banner import print_banner

DATA_DIR = os.path.join(os.path.dirname(__file__), "..", "data")


def load_json(path):
    """Load and return the JSON contents of the given file path."""
    with open(path) as f:
        return json.load(f)


def bucket_score(score):
    """
    Convert a fractional 1.0-5.0 score into an integer grid bucket 1-5,
    using standard rounding, clamped to the [1, 5] range.

    Examples: 3.4 -> 3, 3.6 -> 4, 0.8 -> 1 (clamped), 5.2 -> 5 (clamped).

    TODO: implement this function.
    """
    # HINT: Python's built-in round() gets you most of the way. Then make
    # sure the result can't go below 1 or above 5 -- think about min()/max().
    # raise NotImplementedError("TODO: implement bucket_score")

    bucket = round(score)
    return max(1, min(5, bucket))

def build_grid(register):
    """
    Build a 5x5 grid (list of 5 lists of 5 lists) where
    grid[impact_bucket - 1][likelihood_bucket - 1] is a list of finding
    labels ("host: plugin_name") that land in that cell.

    Row index 0 = impact bucket 1 (lowest), column index 0 = likelihood
    bucket 1 (lowest) -- so grid[4][4] is the "critical/critical" corner.

    TODO: implement this function using bucket_score() on each finding's
    likelihood_score and impact_score.
    """
    # HINT: start with grid = [[[] for _ in range(5)] for _ in range(5)],
    # then loop over `register` and append each finding's label into the
    # right cell.
    # raise NotImplementedError("TODO: implement build_grid")

    grid = [[[] for _ in range(5)] for _ in range(5)]
    for finding in register:
        li = bucket_score(finding["likelihood_score"])
        im = bucket_score(finding["impact_score"])
        label = f"{finding['host']}: {finding['plugin_name']}"
        grid[im - 1][li - 1].append(label)
    return grid


def grid_counts(grid):
    """Return a 5x5 numpy array of finding counts per cell (for coloring)."""
    return np.array([[len(cell) for cell in row] for row in grid])


def plot_heatmap(grid, track_name, out_path):
    """
    Render the 5x5 grid as a heat map: x-axis = Likelihood (1-5),
    y-axis = Impact (1-5), color = finding count, with each cell
    annotated by count and (space permitting) the finding labels.

    Provided for you -- no changes needed here.
    """
    counts = grid_counts(grid)

    fig, ax = plt.subplots(figsize=(8, 7))
    im = ax.imshow(counts, cmap="YlOrRd", origin="lower", vmin=0)

    ax.set_xticks(range(5))
    ax.set_xticklabels([1, 2, 3, 4, 5])
    ax.set_yticks(range(5))
    ax.set_yticklabels([1, 2, 3, 4, 5])
    ax.set_xlabel("Likelihood (1 = low, 5 = high)", fontsize=12)
    ax.set_ylabel("Impact (1 = low, 5 = high)", fontsize=12)
    ax.set_title(f"Risk Heat Map \u2014 {track_name}", fontsize=14, fontweight="bold")

    for im_idx in range(5):
        for li_idx in range(5):
            cell = grid[im_idx][li_idx]
            if not cell:
                continue
            text_color = "white" if counts[im_idx, li_idx] >= counts.max() / 2 else "black"
            label = "\n".join(h.split(":")[0] for h in cell[:3])
            if len(cell) > 3:
                label += f"\n+{len(cell) - 3} more"
            ax.text(li_idx, im_idx, f"{len(cell)}\n{label}", ha="center", va="center",
                     fontsize=7, color=text_color)

    fig.colorbar(im, ax=ax, label="Findings in cell")
    fig.tight_layout()
    fig.savefig(out_path, dpi=150)
    plt.close(fig)


def main():
    parser = argparse.ArgumentParser(description="WS-VSW risk heat map generator")
    parser.add_argument("--track", required=True,
                         choices=["track_a_meridian_health", "track_b_northbridge_financial",
                                  "track_c_coastal_logistics"])
    parser.add_argument("--out", default=None)
    args = parser.parse_args()

    print_banner("Risk Heat Map Generator", [f"Track: {args.track}"])

    track_dir = os.path.join(DATA_DIR, args.track)
    register = load_json(os.path.join(track_dir, "risk_register.json"))
    out_path = args.out or os.path.join(track_dir, "risk_heatmap.png")

    grid = build_grid(register)
    plot_heatmap(grid, args.track, out_path)

    print(f"Heat map for {args.track} written to {out_path}")


if __name__ == "__main__":
    main()
