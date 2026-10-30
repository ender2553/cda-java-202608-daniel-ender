package demo.xss.controller;

import demo.xss.service.ResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reset endpoint. */
@RestController
public class ResetController {

    private final ResetService reset;

    public ResetController(ResetService reset) {
        this.reset = reset;
    }

    @PostMapping("/api/reset")
    public ResponseEntity<Void> reset() {
        reset.resetDemo();
        return ResponseEntity.noContent().build();
    }
}
