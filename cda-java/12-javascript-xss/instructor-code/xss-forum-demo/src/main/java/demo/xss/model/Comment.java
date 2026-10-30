package demo.xss.model;

import java.time.OffsetDateTime;

/**
 * One comment on a topic, with the author's name joined in. The text and the
 * name are exactly as stored — nothing is encoded on the way out.
 */
public record Comment(int commentId, int topicId, int userId, String userName,
                      String commentText, OffsetDateTime commentDateTime) {
}
