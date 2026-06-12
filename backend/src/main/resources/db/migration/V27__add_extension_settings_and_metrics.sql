ALTER TABLE user_setting ADD COLUMN pause_interval_minutes INTEGER;
ALTER TABLE user_setting ADD COLUMN monitored_domains TEXT;

CREATE TABLE extension_metrics (
    id SERIAL PRIMARY KEY,
    id_app_user BIGINT NOT NULL,
    domain VARCHAR(255) NOT NULL,
    active_seconds INTEGER NOT NULL DEFAULT 0,
    scroll_count INTEGER NOT NULL DEFAULT 0,
    modals_shown INTEGER NOT NULL DEFAULT 0,
    redirects INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_extension_metrics_user FOREIGN KEY (id_app_user) REFERENCES app_user(id)
);
