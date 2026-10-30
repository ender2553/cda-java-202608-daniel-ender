package demo.xss.dao;

import demo.xss.model.Topic;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Reads the topics table. */
@Repository
public class TopicDao {

    private static final RowMapper<Topic> MAPPER = (rs, rowNum) ->
            new Topic(rs.getInt("topic_id"), rs.getString("topic_name"));

    private final JdbcTemplate jdbc;

    public TopicDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Topic> findAll() {
        return jdbc.query(
                "SELECT topic_id, topic_name FROM topics ORDER BY topic_id", MAPPER);
    }
}
