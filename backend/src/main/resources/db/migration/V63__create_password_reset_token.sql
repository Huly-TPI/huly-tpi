CREATE TABLE IF NOT EXISTS password_reset_token (
    id          BIGSERIAL PRIMARY KEY,
    id_app_user BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token       VARCHAR(36) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL
);
