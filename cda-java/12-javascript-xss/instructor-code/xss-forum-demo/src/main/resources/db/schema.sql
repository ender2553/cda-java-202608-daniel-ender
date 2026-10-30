-- XSS Forum Demo: schema and seed data.
-- Drops and recreates users, topics and comments, then reloads the seed data.
-- Re-running this script resets everything, including any comments you posted.
-- Run it with:
--   psql -U postgres -d xss_demo -f src/main/resources/db/schema.sql

DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS topics;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL UNIQUE CHECK (user_name <> '')
);

CREATE TABLE topics (
    topic_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    topic_name VARCHAR(100) NOT NULL
);

CREATE TABLE comments (
    comment_id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    topic_id          INTEGER NOT NULL REFERENCES topics (topic_id),
    user_id           INTEGER NOT NULL REFERENCES users (user_id),
    comment_text      TEXT NOT NULL CHECK (comment_text <> ''),
    comment_date_time TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_comments_topic_time ON comments (topic_id, comment_date_time DESC);

-- Seed data
INSERT INTO users (user_name) VALUES ('alice');
INSERT INTO users (user_name) VALUES ('bob');
INSERT INTO users (user_name) VALUES ('carol');
INSERT INTO users (user_name) VALUES ('dave');

INSERT INTO topics (topic_name) VALUES ('General Chat');
INSERT INTO topics (topic_name) VALUES ('Java Help');
INSERT INTO topics (topic_name) VALUES ('Weekend Plans');
INSERT INTO topics (topic_name) VALUES ('Favorite Snacks');

-- General Chat
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'General Chat'),
        (SELECT user_id FROM users WHERE user_name = 'alice'),
        'Welcome to the forum, everyone! Glad to have you all here.',
        now() - interval '3 days');
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'General Chat'),
        (SELECT user_id FROM users WHERE user_name = 'bob'),
        'Thanks Alice. Happy to be here. Looking forward to some good conversations.',
        now() - interval '2 days');
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'General Chat'),
        (SELECT user_id FROM users WHERE user_name = 'carol'),
        'Hello all! How is everyone doing this week?',
        now() - interval '5 hours');

-- Java Help
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Java Help'),
        (SELECT user_id FROM users WHERE user_name = 'dave'),
        'Can someone explain the difference between an interface and an abstract class?',
        now() - interval '2 days');
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Java Help'),
        (SELECT user_id FROM users WHERE user_name = 'alice'),
        'An interface describes what a class can do, while an abstract class can also share some code. A class can implement many interfaces but extend only one class.',
        now() - interval '30 hours');
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Java Help'),
        (SELECT user_id FROM users WHERE user_name = 'dave'),
        'That makes sense now. Thanks, Alice!',
        now() - interval '28 hours');

-- Weekend Plans
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Weekend Plans'),
        (SELECT user_id FROM users WHERE user_name = 'bob'),
        'I am planning a long hike on Saturday if the weather holds up.',
        now() - interval '20 hours');
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Weekend Plans'),
        (SELECT user_id FROM users WHERE user_name = 'carol'),
        'Sounds fun! I will probably catch up on some reading and cook a big dinner.',
        now() - interval '18 hours');
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Weekend Plans'),
        (SELECT user_id FROM users WHERE user_name = 'alice'),
        'Farmers market for me, then maybe a movie in the evening.',
        now() - interval '90 minutes');

-- Favorite Snacks
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Favorite Snacks'),
        (SELECT user_id FROM users WHERE user_name = 'carol'),
        'You cannot beat popcorn with a little salt and butter.',
        now() - interval '2 days');
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Favorite Snacks'),
        (SELECT user_id FROM users WHERE user_name = 'dave'),
        'I am a pretzel and hummus person myself.',
        now() - interval '1 day');
INSERT INTO comments (topic_id, user_id, comment_text, comment_date_time)
VALUES ((SELECT topic_id FROM topics WHERE topic_name = 'Favorite Snacks'),
        (SELECT user_id FROM users WHERE user_name = 'bob'),
        'Apple slices with peanut butter. Simple and tasty.',
        now() - interval '4 hours');
