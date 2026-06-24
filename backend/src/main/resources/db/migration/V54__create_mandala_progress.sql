CREATE TABLE mandala_progress (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    mandala_id VARCHAR(100) NOT NULL REFERENCES mandala(id) ON DELETE CASCADE,
    paint_blob BYTEA NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, mandala_id)
);
