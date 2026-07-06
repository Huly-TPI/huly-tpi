CREATE TABLE IF NOT EXISTS pendiente_task (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    title               VARCHAR(120) NOT NULL,
    description         VARCHAR(1000),
    due_date            DATE,
    estimated_duration  VARCHAR(20),
    category            VARCHAR(20),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    mental_load_score   DOUBLE PRECISION,
    mental_load_bucket  VARCHAR(10),
    position_x          DOUBLE PRECISION,
    position_y          DOUBLE PRECISION,
    rotation_deg        DOUBLE PRECISION,
    pinned_at           TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_pendiente_task_user_status
    ON pendiente_task (user_id, status);

CREATE TABLE IF NOT EXISTS pendiente_subtask (
    id           BIGSERIAL PRIMARY KEY,
    task_id      BIGINT NOT NULL REFERENCES pendiente_task(id) ON DELETE CASCADE,
    text         VARCHAR(200) NOT NULL,
    done         BOOLEAN NOT NULL DEFAULT FALSE,
    position     INT NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pendiente_subtask_task_id
    ON pendiente_subtask (task_id);

CREATE TABLE IF NOT EXISTS pendiente_daily_recommendation (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    recommendation_date DATE NOT NULL,
    decision            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    pending_set_hash    VARCHAR(64) NOT NULL,
    total_load_budget   DOUBLE PRECISION NOT NULL,
    total_load_used     DOUBLE PRECISION NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    decided_at          TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_pendiente_daily_recommendation_user_date UNIQUE (user_id, recommendation_date)
);

CREATE TABLE IF NOT EXISTS pendiente_daily_recommendation_task (
    id                       BIGSERIAL PRIMARY KEY,
    recommendation_id        BIGINT NOT NULL REFERENCES pendiente_daily_recommendation(id) ON DELETE CASCADE,
    task_id                  BIGINT NOT NULL REFERENCES pendiente_task(id) ON DELETE CASCADE,
    CONSTRAINT uq_pendiente_reco_task UNIQUE (recommendation_id, task_id)
);

CREATE INDEX IF NOT EXISTS idx_pendiente_reco_task_reco_id
    ON pendiente_daily_recommendation_task (recommendation_id);
CREATE INDEX IF NOT EXISTS idx_pendiente_reco_task_task_id
    ON pendiente_daily_recommendation_task (task_id);
