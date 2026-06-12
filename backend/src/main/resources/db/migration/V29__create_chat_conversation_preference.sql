CREATE TABLE chat_conversation_preference (
    id BIGSERIAL PRIMARY KEY,
    id_app_user BIGINT NOT NULL,
    preferred_name VARCHAR(50),
    communication_style VARCHAR(40),
    onboarding_status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_chat_conversation_preference_user
        FOREIGN KEY (id_app_user) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_chat_conversation_preference_user UNIQUE (id_app_user)
);

CREATE INDEX idx_chat_conversation_preference_status
    ON chat_conversation_preference (onboarding_status);
