ALTER TABLE user_detail
    ADD COLUMN IF NOT EXISTS profile_onboarding_tutorial_completed BOOLEAN NOT NULL DEFAULT FALSE;
