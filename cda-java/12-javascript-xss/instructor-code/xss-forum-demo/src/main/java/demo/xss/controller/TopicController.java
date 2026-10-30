package demo.xss.controller;

import demo.xss.model.Topic;
import demo.xss.service.TopicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Topic list endpoint. */
@RestController
public class TopicController {

    private final TopicService topics;

    public TopicController(TopicService topics) {
        this.topics = topics;
    }

    @GetMapping("/api/topics")
    public List<Topic> list() {
        return topics.getAllTopics();
    }
}
