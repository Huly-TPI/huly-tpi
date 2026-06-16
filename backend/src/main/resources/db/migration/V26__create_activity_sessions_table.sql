CREATE TABLE IF NOT EXISTS activity_sessions (
    id BIGSERIAL PRIMARY KEY,
    id_app_user BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_activity_sessions_user FOREIGN KEY (id_app_user) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_activity_sessions_id_app_user ON activity_sessions(id_app_user);
CREATE INDEX IF NOT EXISTS idx_activity_sessions_type ON activity_sessions(activity_type);
