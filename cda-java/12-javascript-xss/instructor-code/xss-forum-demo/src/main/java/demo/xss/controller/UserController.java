package demo.xss.controller;

import demo.xss.model.LoginRequest;
import demo.xss.model.User;
import demo.xss.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login endpoint. Sets no cookies — the browser JavaScript stores the
 * returned user in its own cookies.
 */
@RestController
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @PostMapping("/api/users/login")
    public User login(@RequestBody LoginRequest request) {
        return users.login(request.userName());
    }
}
