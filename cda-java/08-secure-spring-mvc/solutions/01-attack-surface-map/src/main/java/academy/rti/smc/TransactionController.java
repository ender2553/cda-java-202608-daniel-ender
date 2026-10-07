package academy.rti.smc;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Baseline REST surface for the attack-surface mapping lab.
 *
 * Each handler below is an entry point for untrusted input. The lab task is to
 * trace where data crosses the trust boundary into the application.
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody TransactionRequest request) {
        Map<String, Object> ack = Map.of(
                "accepted", true,
                "accountId", String.valueOf(request.getAccountId()),
                "amount", String.valueOf(request.getAmount()),
                "currency", String.valueOf(request.getCurrency()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ack);
    }
}
