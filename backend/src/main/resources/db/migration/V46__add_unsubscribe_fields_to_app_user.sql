ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS reengagement_emails_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS unsubscribe_token UUID NOT NULL DEFAULT gen_random_uuid();

ALTER TABLE app_user
    ADD CONSTRAINT uk_app_user_unsubscribe_token UNIQUE (unsubscribe_token);