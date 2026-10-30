package demo.xss.service;

import demo.xss.dao.CommentDao;
import demo.xss.model.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reads and adds comments.
 *
 * <p>The text is saved exactly as typed (no trimming, no cleaning) on purpose:
 * that is what lets a script payload reach the page later.
 */
@Service
public class CommentService {

    private final CommentDao comments;

    public CommentService(CommentDao comments) {
        this.comments = comments;
    }

    public List<Comment> getCommentsForTopic(int topicId) {
        return comments.findByTopic(topicId);
    }

    @Transactional
    public void addComment(int topicId, int userId, String commentText) {
        if (commentText == null || commentText.isBlank()) {
            throw new IllegalArgumentException("Enter a comment.");
        }
        comments.insert(topicId, userId, commentText);
    }
}
