| Item | Location | Type | Authority Checked | Evidence | Decision |
|---|---|---|---|---|---|
| `requests==2.31.0` | `requirements.txt` | Dependency | PyPI | Exact release exists; release provenance is not established by the available evidence. | unresolved |
| `pandas==1.3.5` | `requirements.txt` | Dependency | PyPI | Exact release exists; release files show no Trusted Publishing provenance. | unresolved |
| `python-dateutil==2.8.2` | `requirements.txt` | Dependency | PyPI | Exact release exists; PyPI reports no provenance metadata for its files. | unresolved |
| `boto3==1.34.0` | `requirements.txt` | Dependency | PyPI | Exact release exists; provenance has not been established by the available evidence. | unresolved |
| `fastjsonschema-validator==2.1.0` | `requirements.txt` | Suspected hallucinated/slopsquatted dependency | PyPI | Exact package release was not found. The separate `fastjsonschema` project does not validate this package name. | reject |
| `validate_schema` import | `api_client.py` | Invalid dependency import | PyPI; package documentation | Import depends on the rejected package; no verified evidence supports this import. | reject |
| `requests.Session(retries=3)` | `api_client.py` | Wrong constructor signature | Official Requests documentation | `Session()` takes no retry argument. Retries are configured through an HTTP adapter. ([Requests][1]) | reject |
| `session.get_json()` | `api_client.py` | Hallucinated method | Official Requests documentation | Requests uses `session.get()` followed by `response.json()`. ([Requests][1]) | reject |
| `resp["records"]` | `api_client.py` | Unverified API behavior | Upstream API documentation unavailable | The supplied code does not establish that the upstream API returns a `records` field. | unresolved |
| `date_parser.parse_iso()` | `transform.py` | Hallucinated method | Official python-dateutil 2.8.2 documentation | Documented methods include `parse()` and `isoparse()`; `parse_iso()` is not documented. ([dateutil.readthedocs.io][2]) | reject |
| `_s3.upload_object()` | `storage.py` | Hallucinated method | Official Boto3 S3 client documentation | `put_object()` is the documented S3 client operation; `upload_object()` is not documented. | reject |
| `_s3.put_object()` | `storage.py` | Valid API | Official Boto3 S3 client documentation | Supported S3 operation with the supplied `Bucket`, `Key`, `Body`, and `ContentType` parameters. | verify |
| `df.map()` | `pipeline.py` | Real API / wrong version | Official pandas documentation | `DataFrame.map()` was introduced in pandas 2.1.0; the project pins pandas 1.3.5. | reject |

[1]: https://requests.readthedocs.io/en/latest/api/?utm_source=chatgpt.com "Developer Interface — Requests 2.34.2 documentation"

[2]: https://dateutil.readthedocs.io/en/2.8.2/parser.html?utm_source=chatgpt.com "parser — dateutil 2.8.2 documentation"