package academy.rti.smc;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    /**
     * Create a transaction from a validated request body.
     *
     * <p>A blank {@code accountId} (or non-positive {@code amount}) fails bean validation
     * and raises {@code MethodArgumentNotValidException} before this method body runs.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "accountId", request.accountId(),
                        "amount", request.amount(),
                        "status", "ACCEPTED"));
    }

    /**
     * Fetch a transaction by id.
     *
     * <p>When {@code id} is "boom" this simulates an unexpected internal failure whose
     * message carries sensitive internal detail (a DB connection string). That detail
     * must NEVER reach the client. With no centralized exception handling, Spring's
     * default error handling will leak it (especially with stacktrace/message includes on).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        if ("boom".equals(id)) {
            throw new RuntimeException(
                    "DB connection failed: jdbc:postgresql://prod-db:5432/ledger?user=admin&password=secret-pw-9f3a");
        }
        return ResponseEntity.ok(Map.of("id", id, "status", "FOUND"));
    }
}
