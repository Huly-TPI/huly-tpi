CREATE TABLE mandala_progress (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    mandala_id VARCHAR(255) NOT NULL,
    paint_blob BYTEA,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, mandala_id)
);
