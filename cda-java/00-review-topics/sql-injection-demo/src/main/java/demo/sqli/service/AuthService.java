package demo.sqli.service;

import demo.sqli.model.User;
import demo.sqli.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Business layer for login. Like {@link ProductService}, it passes the raw
 * username and password straight through to the repository.
 */
@Service
public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public User login(String username, String password) {
        return users.findByCredentials(username, password);
    }
}
