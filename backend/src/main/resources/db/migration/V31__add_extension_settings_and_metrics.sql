ALTER TABLE user_setting ADD COLUMN IF NOT EXISTS pause_interval_minutes INTEGER;
ALTER TABLE user_setting ADD COLUMN IF NOT EXISTS monitored_domains TEXT;
ALTER TABLE user_setting ADD COLUMN IF NOT EXISTS data_sharing_consent BOOLEAN DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS extension_metrics (
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

CREATE TABLE IF NOT EXISTS antiscroll_config (
    id SERIAL PRIMARY KEY,
    default_pause_interval_minutes INTEGER NOT NULL DEFAULT 20,
    terms_and_conditions TEXT NOT NULL
);

INSERT INTO antiscroll_config (default_pause_interval_minutes, terms_and_conditions)
VALUES (20, 'El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentración o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. ¡Cero presiones, el ritmo lo marcás vos!');
