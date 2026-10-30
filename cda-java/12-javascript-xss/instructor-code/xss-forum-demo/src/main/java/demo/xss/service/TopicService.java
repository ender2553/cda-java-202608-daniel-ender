package demo.xss.service;

import demo.xss.dao.TopicDao;
import demo.xss.model.Topic;
import org.springframework.stereotype.Service;

import java.util.List;

/** Lists the forum topics. */
@Service
public class TopicService {

    private final TopicDao topics;

    public TopicService(TopicDao topics) {
        this.topics = topics;
    }

    public List<Topic> getAllTopics() {
        return topics.findAll();
    }
}
