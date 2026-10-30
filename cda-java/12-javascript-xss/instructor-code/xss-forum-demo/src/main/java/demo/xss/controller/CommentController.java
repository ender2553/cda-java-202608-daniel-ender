package demo.xss.controller;

import demo.xss.model.Comment;
import demo.xss.model.NewCommentRequest;
import demo.xss.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Comment endpoints. The text goes back out exactly as stored — no output
 * encoding — so a script payload in a comment reaches the browser intact.
 */
@RestController
public class CommentController {

    private final CommentService comments;

    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping("/api/topics/{topicId}/comments")
    public List<Comment> list(@PathVariable int topicId) {
        return comments.getCommentsForTopic(topicId);
    }

    @PostMapping("/api/topics/{topicId}/comments")
    public ResponseEntity<Void> add(@PathVariable int topicId,
                                    @RequestBody NewCommentRequest request) {
        comments.addComment(topicId, request.userId(), request.commentText());
        return ResponseEntity.status(201).build();
    }
}
