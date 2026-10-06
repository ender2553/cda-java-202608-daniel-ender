package academy.rti.smc;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransactionController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody TransactionRequest request) {
        // Pretend to persist; echo back a created resource representation.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "accountId", request.accountId(),
                        "amount", request.amount(),
                        "currency", request.currency()
                ));
    }
}
