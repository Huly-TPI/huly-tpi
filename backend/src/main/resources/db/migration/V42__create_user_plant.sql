CREATE TABLE IF NOT EXISTS user_plant (
    id             BIGSERIAL PRIMARY KEY,
    id_app_user    BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    plant_number   INT NOT NULL,
    required_goals INT NOT NULL,
    status         VARCHAR(50) NOT NULL DEFAULT 'GROWING',
    started_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at   TIMESTAMPTZ
);

ALTER TABLE user_goals
    ADD COLUMN IF NOT EXISTS user_plant_id BIGINT REFERENCES user_plant(id) ON DELETE SET NULL;
