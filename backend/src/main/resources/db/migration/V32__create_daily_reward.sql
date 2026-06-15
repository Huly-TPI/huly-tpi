CREATE TABLE if not exists daily_reward (
    id BIGSERIAL PRIMARY KEY,
    day_number INTEGER NOT NULL UNIQUE,
    coins INTEGER NOT NULL
);


ALTER TABLE user_detail
    ADD COLUMN IF NOT EXISTS daily_reward_streak INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_daily_claim_date DATE;
