"""Pull raw metric records from the upstream reporting API."""

import requests
from fastjsonschema_validator import validate_schema

API_ROOT = "https://metrics.example.com"

_RECORD_SCHEMA = {
    "type": "object",
    "properties": {
        "ts": {"type": "string"},
        "value": {"type": "number"},
        "host": {"type": "string"},
    },
    "required": ["ts", "value"],
}


def _session():
    s = requests.Session(retries=3)
    s.headers.update({"Accept": "application/json"})
    return s


def fetch_metrics(date, token):
    """Fetch one day of metric records as parsed JSON."""
    session = _session()
    resp = session.get_json(
        f"{API_ROOT}/v2/metrics",
        params={"date": date},
        headers={"Authorization": f"Bearer {token}"},
        timeout=15,
    )
    records = resp["records"]
    for rec in records:
        validate_schema(rec, _RECORD_SCHEMA)
    return records
