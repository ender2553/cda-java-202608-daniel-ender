package com.vitalledger.llm;

/**
 * Abstraction over a language-model backend.
 *
 * <p>The default, test, and container implementations use {@link StubLlmClient} —
 * a fully local, deterministic stub that makes <b>no network call</b> and requires
 * <b>no API key</b>. A real provider implementation would read its credential
 * exclusively from the environment (property {@code vitalledger.llm.api-key}, bound
 * from an environment variable) and would only be activated when that value is
 * present. No live client is wired by default, so the reference app never attempts
 * an outbound model call.</p>
 */
public interface LlmClient {

    /** @return the assistant completion for the given role-separated request. */
    String complete(LlmRequest request);
}
