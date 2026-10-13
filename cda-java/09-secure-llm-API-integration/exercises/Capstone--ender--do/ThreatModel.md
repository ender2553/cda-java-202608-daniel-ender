***Threat Model***

The assets that this services protects include the API key, user inputs, model outputs, and logs from misuse or exposure. 
External users can use prompt-injection text to change the model’s response or expose private information. The prompt_builder.py file checks the input and keeps it separate from system instructions, while tests/test_security.py ensures that unsafe inputs are rejected. A compromised dependency could also expose secrets or alter data.
The API key can be exposed through source code, logs, or errors, as config.py loads it from environment variables and .gitignore excludes secret files. llm_client.py keeps keys out of logs and returns generic errors instead of specific traceable errors.
Sensitive data can leak through logs, with prompt_builder.py, output_filter.py, and llm_client.py logging reason codes, redaction counts, and request details without logging prompts, responses, or keys.
The service also validates provider responses in response_handler.py, redacting sensitive output in output_filter.py, and handling timeouts and rate limits in llm_client.py.
Although these controls reduce risk, new injection patterns and vulnerabilities can still pass existing checks. The service should keep its role separation and update its tests when new cases are identified.