ALTER TABLE chat_message
    ADD COLUMN suggested_action_type VARCHAR(50),
    ADD COLUMN suggested_action_activity_id BIGINT,
    ADD COLUMN suggested_action_title VARCHAR(255),
    ADD COLUMN suggested_action_description TEXT,
    ADD COLUMN suggested_action_url VARCHAR(500),
    ADD COLUMN suggested_action_emotional_event_id BIGINT,
    ADD COLUMN suggested_action_decision VARCHAR(50),
    ADD COLUMN generated_challenge_title VARCHAR(255),
    ADD COLUMN generated_challenge_description TEXT,
    ADD COLUMN challenge_decision VARCHAR(50);
