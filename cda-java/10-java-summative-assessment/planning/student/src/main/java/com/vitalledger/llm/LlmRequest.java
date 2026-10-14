package com.vitalledger.llm;

import java.util.List;

/** An immutable, role-separated request to an {@link LlmClient}. */
public record LlmRequest(List<LlmMessage> messages) {

    public LlmRequest {
        messages = List.copyOf(messages); // defensive copy — request is immutable
    }
}
