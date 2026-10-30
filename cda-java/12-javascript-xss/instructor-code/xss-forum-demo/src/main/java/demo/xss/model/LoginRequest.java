package demo.xss.model;

/** JSON body for POST /api/users/login. */
public record LoginRequest(String userName) {
}
