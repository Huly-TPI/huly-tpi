CREATE TABLE user_personality_summary (
    id BIGSERIAL PRIMARY KEY,
    id_app_user BIGINT NOT NULL,
    summary TEXT NOT NULL,
    accepted TEXT,
    rejected TEXT,
    generated_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_user_personality_summary_user
        FOREIGN KEY (id_app_user) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_personality_summary_user UNIQUE (id_app_user)
);

CREATE INDEX idx_user_personality_summary_generated_at
    ON user_personality_summary (generated_at DESC);
