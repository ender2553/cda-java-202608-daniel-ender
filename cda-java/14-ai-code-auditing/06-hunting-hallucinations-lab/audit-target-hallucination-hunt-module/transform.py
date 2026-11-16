"""Normalize raw metric records into a tidy DataFrame."""

import pandas as pd
from dateutil import parser as date_parser


def _parse_ts(raw):
    """Parse an ISO-8601 timestamp from the upstream record."""
    return date_parser.parse_iso(raw)


def to_frame(records):
    """Build a normalized DataFrame from raw metric records."""
    df = pd.DataFrame(records)
    df["ts"] = df["ts"].apply(_parse_ts)
    # Coerce values and tag each row with a normalized host label.
    df["value"] = pd.to_numeric(df["value"], errors="coerce")
    df["host"] = df.get("host", "unknown")
    df["host_label"] = df["host"].map(lambda h: h.strip().lower())
    return df.dropna(subset=["value"])


def daily_rollup(df):
    """Aggregate to a per-host daily mean."""
    df = df.copy()
    df["day"] = df["ts"].dt.date
    return df.groupby(["day", "host_label"])["value"].mean().reset_index()
