package com.vitalledger.llm;

/**
 * A single role-tagged message. Role separation is a prompt-injection control:
 * trusted instructions live in a {@code SYSTEM} message; untrusted record content
 * is confined to a {@code USER} message and never concatenated into the system role.
 */
public record LlmMessage(Role role, String content) {

    public enum Role { SYSTEM, USER, ASSISTANT }

    public static LlmMessage system(String content) {
        return new LlmMessage(Role.SYSTEM, content);
    }

    public static LlmMessage user(String content) {
        return new LlmMessage(Role.USER, content);
    }
}
