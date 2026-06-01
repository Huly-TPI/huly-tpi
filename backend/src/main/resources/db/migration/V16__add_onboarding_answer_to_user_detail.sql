ALTER TABLE user_detail
    ADD COLUMN IF NOT EXISTS onboarding_answer_1 TEXT,
    ADD COLUMN IF NOT EXISTS onboarding_answer_2 TEXT,
    ADD COLUMN IF NOT EXISTS onboarding_answer_3 TEXT;