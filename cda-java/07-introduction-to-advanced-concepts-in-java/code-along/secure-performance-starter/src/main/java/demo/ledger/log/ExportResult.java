package demo.ledger.log;

import java.nio.file.Path;

/** Where an export was written and how many transactions it wrote. */
public record ExportResult(Path file, long transactions) {
}
