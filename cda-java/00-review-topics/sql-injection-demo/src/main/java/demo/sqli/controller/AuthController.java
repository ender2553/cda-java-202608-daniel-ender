package demo.sqli.controller;

import demo.sqli.model.User;
import demo.sqli.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for login. Accepts a username and a clear-text password and
 * returns whether they matched. Used to prove that credentials stolen through
 * the search box are real (and that overwriting them via the search box locks
 * people out / lets an attacker in).
 */
@RestController
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    public record LoginResponse(boolean success, String username, String role) {
    }

    @PostMapping("/api/login")
    public LoginResponse login(@RequestParam String username, @RequestParam String password) {
        User user = auth.login(username, password);
        if (user == null) {
            return new LoginResponse(false, null, null);
        }
        return new LoginResponse(true, user.getUsername(), user.getRole());
    }
}
