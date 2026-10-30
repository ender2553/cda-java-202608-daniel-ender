package demo.xss.service;

import demo.xss.dao.UserDao;
import demo.xss.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login = find the user, or create them. No password.
 *
 * <p>The name is saved exactly as typed (no trimming, no cleaning) on purpose:
 * that is what lets a script payload reach the pages later.
 */
@Service
public class UserService {

    private final UserDao users;

    public UserService(UserDao users) {
        this.users = users;
    }

    @Transactional
    public User login(String userName) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("Enter a username.");
        }
        return users.findByName(userName).orElseGet(() -> users.insert(userName));
    }
}
