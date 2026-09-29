package com.rti.ledgercore.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reads LedgerCore's runtime settings: a real environment variable wins, otherwise the value
 * comes from a {@code .env} file in the working directory (the project root when run from an IDE
 * or Maven).
 *
 * <p>{@code .env} is read at runtime and is git-ignored, so it never ends up in source control or
 * in the built jar. Commit {@code .env.example} only.
 */
public final class Env {

    private static final Path DOT_ENV = Path.of(".env");
    private static final Map<String, String> FILE_VALUES = loadDotEnv(DOT_ENV);

    private Env() {
    }

    public static Optional<String> get(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = FILE_VALUES.get(name);
        }
        return Optional.ofNullable(value).filter(v -> !v.isBlank());
    }

    public static String require(String name) {
        return get(name).orElseThrow(() -> new IllegalStateException(
                name + " is not set — add it to .env (see .env.example) or the environment"));
    }

    /** Parses simple {@code KEY=value} lines; blank lines and {@code #} comments are skipped. */
    private static Map<String, String> loadDotEnv(Path path) {
        Map<String, String> values = new HashMap<>();
        if (!Files.isRegularFile(path)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                int equals = trimmed.indexOf('=');
                if (trimmed.isEmpty() || trimmed.startsWith("#") || equals <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, equals).trim();
                String value = stripQuotes(trimmed.substring(equals + 1).trim());
                values.put(key, value);
            }
        } catch (IOException readFailure) {
            throw new UncheckedIOException("Failed to read " + path.toAbsolutePath(), readFailure);
        }
        return values;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
