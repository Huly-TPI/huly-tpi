ALTER TABLE user_goals
    ADD COLUMN IF NOT EXISTS activity_id BIGINT,
    ADD CONSTRAINT fk_user_goals_activity
        FOREIGN KEY (activity_id) REFERENCES activity(id)
        ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_user_goals_activity_id ON user_goals(activity_id);
