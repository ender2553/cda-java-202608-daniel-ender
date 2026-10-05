package academy.rti.smc;

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
     * <p>STARTER STATE: the request body is bound WITHOUT validation. There is
     * no {@code @Valid} on the parameter, so the constraints (once you add them
     * to {@link TransactionRequest}) would not even be triggered. Right now every
     * request — well-formed or garbage — is accepted.
     *
     * <p>TODO: add Bean Validation — annotate the parameter with {@code @Valid}
     * (after adding constraints to {@link TransactionRequest}). A failed
     * constraint then produces a {@code MethodArgumentNotValidException}, which
     * Spring maps to HTTP 400 automatically.
     */
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody TransactionRequest request) {
        // In a real service we would persist the transaction here.
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
