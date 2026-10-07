package academy.rti.smc;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    /**
     * Accepts a transaction and returns 201 Created.
     *
     * <p>SOLUTION STATE: the request body is validated via {@code @Valid}. If any
     * constraint on {@link TransactionRequest} fails, Spring raises a
     * {@code MethodArgumentNotValidException} and returns HTTP 400 before this
     * method body runs. Only well-formed transactions reach the 201 path.
     */
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody TransactionRequest request) {
        // In a real service we would persist the transaction here.
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
