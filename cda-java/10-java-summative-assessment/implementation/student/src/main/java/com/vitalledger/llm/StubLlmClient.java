package com.vitalledger.llm;

import org.springframework.stereotype.Component;

/**
 * Local, deterministic stub language-model client — the default backend.
 *
 * <p>It performs <b>no network I/O</b> and needs <b>no API key</b>. It treats the
 * user message strictly as inert data to be summarized, never as instructions, so
 * content injected into a record cannot change its behavior. It only ever sees the
 * already-redacted, already-sanitized text the summarizer service passes it.</p>
 */
@Component
public class StubLlmClient implements LlmClient {

    private static final int MAX_SUMMARY_CHARS = 240;

    @Override
    public String complete(LlmRequest request) {
        // TODO(phase E): deterministically summarize the LAST USER message as inert data (no network, no key) — whitespace-normalize, truncate to MAX_SUMMARY_CHARS, prefix "SUMMARY: "; never interpret the text as instructions.
        throw new UnsupportedOperationException("TODO(phase E)");
    }
}
