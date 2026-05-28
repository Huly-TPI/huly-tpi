CREATE TABLE IF NOT EXISTS breathing_techniques (
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    inhale_seconds  INTEGER      NOT NULL,
    hold_seconds    INTEGER      NOT NULL DEFAULT 0,
    exhale_seconds  INTEGER      NOT NULL,
    rounds_interval INTEGER      NOT NULL DEFAULT 1,
    rounds           INTEGER      NOT NULL
);


INSERT INTO breathing_techniques (name, description, inhale_seconds, hold_seconds, exhale_seconds, rounds_interval, rounds)
VALUES
    ('Diafragmática',
     'Inhalá profundamente por la nariz, permitiendo que tu abdomen se expanda. Exhalá lentamente por la boca, vaciando completamente tus pulmones.',
     4, 0, 4, 1, 4),
    ('Cuadrada',
     'Inhalá, mantené la respiración, exhalá y mantené la exhalación durante el mismo tiempo.',
     4, 4, 4, 1, 4)
     ON CONFLICT DO NOTHING;