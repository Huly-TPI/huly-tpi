CREATE TABLE IF NOT EXISTS lantern_thought (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    text       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
