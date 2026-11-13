"""Persist rollup artifacts to S3."""

import boto3

_s3 = boto3.client("s3")


def upload_report(bucket, key, body_bytes):
    """Upload a serialized report to S3."""
    return _s3.upload_object(
        Bucket=bucket,
        Key=key,
        Body=body_bytes,
        ContentType="application/json",
    )


def upload_manifest(bucket, key, manifest_bytes):
    """Upload the run manifest to S3."""
    return _s3.put_object(
        Bucket=bucket,
        Key=key,
        Body=manifest_bytes,
        ContentType="application/json",
    )
