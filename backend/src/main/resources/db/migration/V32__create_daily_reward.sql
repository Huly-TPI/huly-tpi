CREATE TABLE if not exists daily_reward (
    id BIGSERIAL PRIMARY KEY,
    day_number INTEGER NOT NULL UNIQUE,
    coins INTEGER NOT NULL
);

INSERT INTO daily_reward (day_number, coins) VALUES
    (1, 10),
    (2, 15),
    (3, 20),
    (4, 25),
    (5, 30),
    (6, 40),
    (7, 100)
ON CONFLICT (day_number) DO NOTHING;


ALTER TABLE user_detail
    ADD COLUMN IF NOT EXISTS daily_reward_streak INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_daily_claim_date DATE;
