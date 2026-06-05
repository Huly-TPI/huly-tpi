CREATE TABLE IF NOT EXISTS activity (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL, 
    valence_min DOUBLE PRECISION NOT NULL,
    valence_max DOUBLE PRECISION NOT NULL,
    arousal_min DOUBLE PRECISION NOT NULL,
    arousal_max DOUBLE PRECISION NOT NULL,
    dominance_min DOUBLE PRECISION NOT NULL,
    dominance_max DOUBLE PRECISION NOT NULL,
    effect_valence DOUBLE PRECISION NOT NULL,
    effect_arousal DOUBLE PRECISION NOT NULL,
    effect_dominance DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS user_emotional_state(
    id BIGSERIAL PRIMARY KEY, 
    user_id BIGINT NOT NULL,
    valence DOUBLE PRECISION NOT NULL,
    arousal DOUBLE PRECISION NOT NULL,
    dominance DOUBLE PRECISION NOT NULL,
    intensity DOUBLE PRECISION NOT NULL,
    source VARCHAR(50) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL
);

INSERT INTO activity (type, valence_min, valence_max, arousal_min, arousal_max, dominance_min, dominance_max, effect_valence, effect_arousal, effect_dominance)
VALUES
  ('RESPIRACION', -1.0,  0.3,  0.6,  1.0, -1.0,  1.0,  0.30, -0.30,  0.10),
  ('DIARIO',      -1.0,  0.0, -0.5,  0.5, -0.5,  1.0,  0.35, -0.10,  0.20),
  ('NUBE',        -1.0,  0.2, -0.3,  1.0, -1.0,  1.0,  0.20, -0.20,  0.10),
  ('BURBUJA',     -1.0,  0.3, -1.0,  1.0, -1.0,  0.5,  0.25,  0.05,  0.10);
