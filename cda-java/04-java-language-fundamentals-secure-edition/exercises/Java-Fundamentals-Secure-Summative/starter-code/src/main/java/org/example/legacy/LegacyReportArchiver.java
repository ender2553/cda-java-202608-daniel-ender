package org.example.legacy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * STUDENT TODO VERSION.
 *
 * This class originally shipped with one bug. Your job is to write a
 * correct implementation from scratch -- use the bug description below
 * as a reminder of the mistake NOT to reproduce:
 *
 *   BUG 1 (resource leak on failure path): opened a FileOutputStream
 *          and FileInputStream manually (`new FileInputStream(...)`,
 *          `new FileOutputStream(...)`) and only called `.close()` on
 *          both at the very end of the method body -- so if
 *          `in.read(buffer)` or `out.write(buffer)` threw partway
 *          through (e.g., disk full, permission revoked mid-copy), BOTH
 *          streams were leaked because the close() calls were never
 *          reached.
 *
 * REQUIRED FIX: try-with-resources (or, where that's not available for a
 * given API, a try/finally that closes in the finally block) guarantees
 * cleanup on every exit path, including exceptions.
 */
public final class LegacyReportArchiver {

    /**
     * TODO: copy a report file into an archive location, verifying it is
     * non-empty first.
     *   - If Files.exists(source) is false, throw
     *     new IOException("Source report does not exist: " + source).
     *   - Otherwise, copy source to archiveDestination. The simplest
     *     correct approach is Files.copy(source, archiveDestination,
     *     StandardCopyOption.REPLACE_EXISTING) -- NIO's Files.copy
     *     manages its own resources internally with no handles left in
     *     your control, which eliminates this bug class entirely. (Where
     *     a manual stream copy is ever unavoidable elsewhere in a real
     *     project, wrap the streams in try-with-resources instead:
     *       try (InputStream in = Files.newInputStream(source);
     *            OutputStream out = Files.newOutputStream(dest)) {
     *           in.transferTo(out);
     *       }
     *     -- but for THIS method, Files.copy(...) is sufficient.)
     *   - After copying, defensively re-check that the archived copy is
     *     non-empty: open it with try-with-resources
     *     (Files.newBufferedReader(archiveDestination, StandardCharsets.UTF_8))
     *     and if reader.readLine() returns null, throw
     *     new IOException("Archived report appears to be empty: " + archiveDestination).
     */
    public void archiveReport(Path source, Path archiveDestination) throws IOException {
        throw new UnsupportedOperationException("TODO: implement archiveReport()");
    }
}
