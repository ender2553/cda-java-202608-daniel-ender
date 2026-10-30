package demo.xss.dao;

import demo.xss.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Reads and writes the comments table. SQL uses ? parameters, so there is no
 * SQL injection here — this demo is about XSS.
 */
@Repository
public class CommentDao {

    private static final RowMapper<Comment> MAPPER = (rs, rowNum) ->
            new Comment(
                    rs.getInt("comment_id"),
                    rs.getInt("topic_id"),
                    rs.getInt("user_id"),
                    rs.getString("user_name"),
                    rs.getString("comment_text"),
                    rs.getObject("comment_date_time", OffsetDateTime.class));

    private final JdbcTemplate jdbc;

    public CommentDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Newest first; comment_id breaks ties. */
    public List<Comment> findByTopic(int topicId) {
        return jdbc.query(
                "SELECT c.comment_id, c.topic_id, c.user_id, u.user_name, "
                        + "c.comment_text, c.comment_date_time "
                        + "FROM comments c JOIN users u ON u.user_id = c.user_id "
                        + "WHERE c.topic_id = ? "
                        + "ORDER BY c.comment_date_time DESC, c.comment_id DESC",
                MAPPER, topicId);
    }

    /** The database default sets the time. */
    public void insert(int topicId, int userId, String commentText) {
        jdbc.update(
                "INSERT INTO comments (topic_id, user_id, comment_text) VALUES (?, ?, ?)",
                topicId, userId, commentText);
    }
}
