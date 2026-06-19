ALTER TABLE user_setting ADD COLUMN IF NOT EXISTS pause_interval_seconds INTEGER;

UPDATE user_setting
SET pause_interval_seconds = COALESCE(pause_interval_seconds, pause_interval_minutes * 60)
WHERE pause_interval_seconds IS NULL
  AND pause_interval_minutes IS NOT NULL;
