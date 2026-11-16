"""Wire the pipeline end to end: fetch -> transform -> roll up -> store."""

import json

from api_client import fetch_metrics
from storage import upload_manifest, upload_report
from transform import daily_rollup, to_frame


def run(date, token, bucket):
    """Run the full daily aggregation pipeline."""
    records = fetch_metrics(date, token)
    df = to_frame(records)

    # Round every numeric cell to two places before rollup.
    df = df.map(lambda v: round(v, 2) if isinstance(v, float) else v)

    rollup = daily_rollup(df)
    body = rollup.to_json(orient="records").encode("utf-8")

    report_key = f"reports/{date}.json"
    upload_report(bucket, report_key, body)

    manifest = json.dumps(
        {"date": date, "rows": int(rollup.shape[0]), "report_key": report_key},
        indent=2,
    ).encode("utf-8")
    upload_manifest(bucket, f"manifests/{date}.json", manifest)

    return report_key
