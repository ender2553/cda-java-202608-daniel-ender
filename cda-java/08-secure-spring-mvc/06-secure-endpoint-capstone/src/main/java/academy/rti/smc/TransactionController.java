package academy.rti.smc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TransactionController {

    // Public health probe — no authentication required (test #6).
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    // Create a transaction.
    //
    // TODO (Bean Validation): annotate the body parameter with @Valid so the
    //      constraints on TransactionRequest are enforced and a bad body yields
    //      400 instead of 201. As written there is NO @Valid, so test #4 FAILS.
    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> create(@RequestBody TransactionRequest request) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> body = Map.of(
                "id", id,
                "accountId", String.valueOf(request.accountId()),
                "amount", String.valueOf(request.amount()),
                "currency", String.valueOf(request.currency()),
                "status", "ACCEPTED"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // Fetch a transaction by id. The id "boom" triggers an internal failure
    // that simulates a leaked backend error.
    //
    // TODO (Safe error handling): there is NO @ControllerAdvice in this starter,
    //      so this exception escapes to the default error path and test #5
    //      (generic 500 problem+json with NO internal detail) FAILS — the
    //      message and class name leak.
    @GetMapping("/transactions/{id}")
    public Map<String, Object> getById(@PathVariable String id) {
        if ("boom".equals(id)) {
            throw new IllegalStateException(
                    "jdbc connection to ledger failed: secret datasource credentials rejected");
        }
        return Map.of("id", id, "status", "ACCEPTED");
    }
}
