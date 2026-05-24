ALTER TABLE risk_word DROP CONSTRAINT IF EXISTS risk_word_word_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_risk_word_lower_word ON risk_word (lower(word));
