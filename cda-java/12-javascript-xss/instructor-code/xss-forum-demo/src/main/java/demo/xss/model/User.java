package demo.xss.model;

/** A forum user. The name is stored exactly as typed. */
public record User(int userId, String userName) {
}
