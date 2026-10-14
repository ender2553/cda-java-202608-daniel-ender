package com.vitalledger.llm;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Prompt-injection defense for untrusted record content.
 *
 * <p>Two controls: (1) a <b>lexical guard</b> that detects common instruction-override
 * markers ("ignore previous instructions", "system:", "disregard the above", …), and
 * (2) <b>neutralization</b> — the guard collapses those markers and fences the content
 * so the model receives it as clearly delimited data, not as instructions. Combined
 * with role separation (content only ever rides in a USER message), a note cannot
 * hijack the system prompt.</p>
 */
@Component
public class PromptGuard {

    private static final List<Pattern> INJECTION_MARKERS = List.of(
            Pattern.compile("ignore\\s+(all\\s+|the\\s+)?(previous|prior|above)\\s+instructions?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("disregard\\s+(the\\s+)?(above|previous|prior|system)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("forget\\s+(everything|all|your)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(^|\\n)\\s*system\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(^|\\n)\\s*assistant\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("new\\s+instructions?\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("reveal\\s+(the\\s+)?(system\\s+prompt|patient|phi)", Pattern.CASE_INSENSITIVE)
    );

    private static final String FENCE = "-----";

    public record Result(String sanitized, boolean injectionDetected) {}

    public Result sanitize(String content) {
        // TODO(phase E): detect + neutralize INJECTION_MARKERS, strip fence break-outs, and return a Result(sanitizedText, injectionDetected).
        throw new UnsupportedOperationException("TODO(phase E)");
    }

    /** Wraps sanitized content in an explicit, model-facing data fence. */
    public String fence(String sanitized) {
        // TODO(phase E): wrap the sanitized content in an explicit BEGIN/END data fence so the model treats it as data, not instructions.
        throw new UnsupportedOperationException("TODO(phase E)");
    }
}
