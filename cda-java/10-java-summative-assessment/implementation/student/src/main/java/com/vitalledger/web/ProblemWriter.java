package com.vitalledger.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 * Serializes RFC-7807 {@code application/problem+json} bodies with a stable error
 * {@code code} and a {@code correlationId}. The correlation id is logged server-side
 * alongside full detail; the response body carries only the safe, generic fields.
 */
final class ProblemWriter {

    private static final Logger log = LoggerFactory.getLogger(ProblemWriter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProblemWriter() {
    }

    static void write(HttpServletResponse response, int status, String code,
                      String title, String detail) throws IOException {
        // TODO(phase D): write an RFC-7807 application/problem+json body with only safe fields (type/title/status/detail/code/correlationId); log the correlation id server-side.
        throw new UnsupportedOperationException("TODO(phase D)");
    }
}
