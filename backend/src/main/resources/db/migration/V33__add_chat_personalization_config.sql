ALTER TABLE chat_config
    ADD COLUMN preferred_name_question_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN communication_style_question_enabled BOOLEAN NOT NULL DEFAULT TRUE;
