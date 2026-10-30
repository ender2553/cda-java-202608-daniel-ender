package demo.xss.model;

/** JSON body for posting a comment. */
public record NewCommentRequest(int userId, String commentText) {
}
