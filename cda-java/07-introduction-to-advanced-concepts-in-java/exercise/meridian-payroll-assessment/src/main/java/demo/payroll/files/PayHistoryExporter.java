package demo.payroll.files;

import demo.payroll.domain.ExportResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PayHistoryExporter {

    private final JdbcTemplate jdbc;
    private final DataFiles files;

    public PayHistoryExporter(JdbcTemplate jdbc, DataFiles files) {
        this.jdbc = jdbc;
        this.files = files;
    }

    public ExportResult export(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both export dates are required.");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must not be after to date.");
        }

        files.ensureDirectory();
        Path out = files.payHistoryFile(from, to);

        AtomicLong rowsWritten = new AtomicLong();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(out.toFile()))) {

            writer.write("employee_id,first_name,last_name,pay_date,gross,net");
            writer.newLine();

            jdbc.query(
                    """
                    SELECT p.employee_id,
                           e.first_name,
                           e.last_name,
                           p.pay_date,
                           p.gross,
                           p.net
                    FROM pay_stubs p
                    JOIN employees e ON e.employee_id = p.employee_id
                    WHERE p.pay_date BETWEEN ? AND ?
                    ORDER BY p.pay_date, p.employee_id
                    """,
                    rs -> {
                        try {
                            writer.write(csv(rs.getString("employee_id")));
                            writer.write(",");
                            writer.write(csv(rs.getString("first_name")));
                            writer.write(",");
                            writer.write(csv(rs.getString("last_name")));
                            writer.write(",");
                            writer.write(csv(rs.getDate("pay_date").toString()));
                            writer.write(",");
                            writer.write(csv(rs.getBigDecimal("gross").toString()));
                            writer.write(",");
                            writer.write(csv(rs.getBigDecimal("net").toString()));
                            writer.newLine();

                            rowsWritten.incrementAndGet();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    },
                    Date.valueOf(from),
                    Date.valueOf(to)
            );

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return new ExportResult(out, rowsWritten.get());
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}