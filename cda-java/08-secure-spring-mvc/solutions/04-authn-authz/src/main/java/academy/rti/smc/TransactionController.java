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

    /**
     * Public health probe. Stays open to anonymous callers in both trees.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /**
     * Records a transaction. The payload is validated (carried-forward control);
     * who is allowed to call it is decided by the SecurityFilterChain.
     */
    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody TransactionRequest request) {
        Map<String, Object> body = Map.of(
                "account", request.account(),
                "amount", request.amount(),
                "recorded", true
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
