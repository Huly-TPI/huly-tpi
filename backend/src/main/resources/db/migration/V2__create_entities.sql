-- PostgreSQL
-- Notas:
-- - Campos cortos => VARCHAR.
-- - Campos largos/libres => TEXT.
-- - Campos Date usados como TIMESTAMPTZ salvo birth, que se define como DATE.
-- - PK => BIGSERIAL.
-- - FKs inferidas por nombre de columna y relaciones del diagrama.

CREATE TABLE IF NOT EXISTS app_user (
                                        id BIGSERIAL PRIMARY KEY,
                                        password VARCHAR(255),
    email VARCHAR(255),
    role VARCHAR(50),
    status VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS user_detail (
                                           id BIGSERIAL PRIMARY KEY,
                                           id_app_user BIGINT NOT NULL,
                                           name VARCHAR(100),
    lastname VARCHAR(100),
    avatar_url VARCHAR(500),
    birth DATE,
    on_boarding_completed BOOLEAN,
    profile_on_boarding_completed BOOLEAN,
    avatar_url_2 VARCHAR(500),
    last_login_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    CONSTRAINT fk_user_detail_app_user
    FOREIGN KEY (id_app_user) REFERENCES app_user(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS refresh_token (
                                             id BIGSERIAL PRIMARY KEY,
                                             id_app_user BIGINT NOT NULL,
                                             created_at TIMESTAMPTZ,
                                             expired_at TIMESTAMPTZ,
                                             ip_address VARCHAR(100),
    CONSTRAINT fk_refresh_token_app_user
    FOREIGN KEY (id_app_user) REFERENCES app_user(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS user_setting (
                                            id BIGSERIAL PRIMARY KEY,
                                            id_app_user BIGINT NOT NULL,
                                            music_volume INTEGER,
                                            effect_volume INTEGER,
                                            anti_scroll_enabled BOOLEAN,
                                            darkmode BOOLEAN,
                                            CONSTRAINT fk_user_setting_app_user
                                            FOREIGN KEY (id_app_user) REFERENCES app_user(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS user_goals (
                                          id BIGSERIAL PRIMARY KEY,
                                          id_app_user BIGINT NOT NULL,
                                          title VARCHAR(255),
    description TEXT,
    status VARCHAR(50),
    created_at TIMESTAMPTZ,
    CONSTRAINT fk_user_goals_app_user
    FOREIGN KEY (id_app_user) REFERENCES app_user(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS breathing_techniques (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    name VARCHAR(150),
    description TEXT,
    inhale_seconds INTEGER,
    hold_seconds INTEGER,
    exhale_seconds INTEGER,
    rounds_interval INTEGER,
    rounds INTEGER
    );

CREATE TABLE IF NOT EXISTS breathing_sessions (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  technique_id BIGINT NOT NULL,
                                                  id_app_user BIGINT NOT NULL,
                                                  created_at TIMESTAMPTZ,
                                                  CONSTRAINT fk_breathing_sessions_technique
                                                  FOREIGN KEY (technique_id) REFERENCES breathing_techniques(id)
    ON DELETE RESTRICT,
    CONSTRAINT fk_breathing_sessions_app_user
    FOREIGN KEY (id_app_user) REFERENCES app_user(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS journal (
                                       id BIGSERIAL PRIMARY KEY,
                                       id_app_user BIGINT NOT NULL,
                                       mood VARCHAR(50),
    title VARCHAR(255),
    CONSTRAINT fk_journal_app_user
    FOREIGN KEY (id_app_user) REFERENCES app_user(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS journal_entries (
                                               id BIGSERIAL PRIMARY KEY,
                                               id_journal BIGINT NOT NULL,
                                               mood VARCHAR(50),
    title VARCHAR(255),
    content TEXT,
    created_at TIMESTAMPTZ,
    CONSTRAINT fk_journal_entries_journal
    FOREIGN KEY (id_journal) REFERENCES journal(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS chat_session (
                                            id BIGSERIAL PRIMARY KEY,
                                            id_app_user BIGINT NOT NULL,
                                            start_at TIMESTAMPTZ,
                                            end_at TIMESTAMPTZ,
                                            CONSTRAINT fk_chat_session_app_user
                                            FOREIGN KEY (id_app_user) REFERENCES app_user(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS chat_message (
                                            id BIGSERIAL PRIMARY KEY,
                                            id_chat_session BIGINT,
                                            message TEXT,
                                            content TEXT,
                                            risk_detected BOOLEAN,
                                            created_at TIMESTAMPTZ,
                                            CONSTRAINT fk_chat_message_chat_session
                                            FOREIGN KEY (id_chat_session) REFERENCES chat_session(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS emotion (
                                       id BIGSERIAL PRIMARY KEY,
                                       id_chat_message BIGINT,
                                       emotion_detected VARCHAR(80),
    CONSTRAINT fk_emotion_chat_message
    FOREIGN KEY (id_chat_message) REFERENCES chat_message(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS chat_config (
                                           id BIGSERIAL PRIMARY KEY,
                                           risk_detection_enabled BOOLEAN,
                                           system_prompt TEXT
);

CREATE INDEX IF NOT EXISTS idx_user_detail_id_app_user ON user_detail(id_app_user);
CREATE INDEX IF NOT EXISTS idx_refresh_token_id_app_user ON refresh_token(id_app_user);
CREATE INDEX IF NOT EXISTS idx_user_setting_id_app_user ON user_setting(id_app_user);
CREATE INDEX IF NOT EXISTS idx_user_goals_id_app_user ON user_goals(id_app_user);
CREATE INDEX IF NOT EXISTS idx_breathing_sessions_technique_id ON breathing_sessions(technique_id);
CREATE INDEX IF NOT EXISTS idx_breathing_sessions_id_app_user ON breathing_sessions(id_app_user);
CREATE INDEX IF NOT EXISTS idx_journal_id_app_user ON journal(id_app_user);
CREATE INDEX IF NOT EXISTS idx_journal_entries_id_journal ON journal_entries(id_journal);
CREATE INDEX IF NOT EXISTS idx_chat_session_id_app_user ON chat_session(id_app_user);
CREATE INDEX IF NOT EXISTS idx_chat_message_id_chat_session ON chat_message(id_chat_session);
CREATE INDEX IF NOT EXISTS idx_emotion_id_chat_message ON emotion(id_chat_message);