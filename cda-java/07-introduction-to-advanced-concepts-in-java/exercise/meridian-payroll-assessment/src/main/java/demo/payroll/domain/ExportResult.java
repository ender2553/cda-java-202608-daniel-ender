package demo.payroll.domain;

import java.nio.file.Path;

public record ExportResult(Path path, long rows) {
}
