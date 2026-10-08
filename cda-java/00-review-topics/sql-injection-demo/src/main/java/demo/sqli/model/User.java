package demo.sqli.model;

/**
 * An application user. Note the clear-text {@code password} field — this app
 * stores and compares passwords with no hashing or encryption (on purpose, so
 * the damage from reading this table is obvious).
 */
public class User {

    private String username;
    private String password;
    private String role;
    private String email;

    public User() {
    }

    public User(String username, String password, String role, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
