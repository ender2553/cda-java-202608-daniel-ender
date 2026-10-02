package demo.payroll.files;

import demo.payroll.domain.ExportResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PayHistoryExporter {

    private final JdbcTemplate jdbc;
    private final DataFiles files;

    public PayHistoryExporter(JdbcTemplate jdbc, DataFiles files) {
        this.jdbc = jdbc;
        this.files = files;
    }

    public ExportResult export(LocalDate from, LocalDate to) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM pay_stubs p JOIN employees e USING (employee_id) WHERE pay_date BETWEEN ? AND ?",
                java.sql.Date.valueOf(from), java.sql.Date.valueOf(to));
        files.ensureDirectory();
        Path out = files.payHistoryFile(from, to);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(out.toFile()));
            if (!rows.isEmpty()) {
                writer.write(String.join(",", rows.get(0).keySet()) + "\n");
            }
            for (Map<String, Object> row : rows) {
                writer.write(row.values().stream().map(String::valueOf).collect(Collectors.joining(",")) + "\n");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new ExportResult(out, rows.size());
    }
}
