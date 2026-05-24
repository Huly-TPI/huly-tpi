CREATE TABLE IF NOT EXISTS risk_word (
    id          BIGSERIAL    PRIMARY KEY,
    word        VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    severity    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_risk_word_word     ON risk_word (word);
CREATE INDEX IF NOT EXISTS idx_risk_word_severity ON risk_word (severity);
CREATE INDEX IF NOT EXISTS idx_risk_word_active   ON risk_word (active);
