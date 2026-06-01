CREATE TABLE IF NOT EXISTS emotional_event (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    source VARCHAR(50) NOT NULL,
    input_text TEXT,
    detected_emotion VARCHAR(100) NOT NULL,
    confidence DOUBLE PRECISION,
    valence DOUBLE PRECISION,
    arousal DOUBLE PRECISION,
    dominance DOUBLE PRECISION,
    intensity DOUBLE PRECISION,
    user_goal VARCHAR(255),
    generated_recommendation TEXT,
    recommended_activity_id BIGINT,
    chosen_activity_id BIGINT,
    recommendation_decision VARCHAR(50),
    feedback_score INTEGER,
    feedback_text TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emotional_event_app_user
        FOREIGN KEY (user_id) REFERENCES app_user(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_emotional_event_recommended_activity
        FOREIGN KEY (recommended_activity_id) REFERENCES activity(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_emotional_event_chosen_activity
        FOREIGN KEY (chosen_activity_id) REFERENCES activity(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_emotional_event_confidence
        CHECK (confidence IS NULL OR (confidence >= 0.0 AND confidence <= 1.0)),
    CONSTRAINT chk_emotional_event_valence
        CHECK (valence IS NULL OR (valence >= -1.0 AND valence <= 1.0)),
    CONSTRAINT chk_emotional_event_arousal
        CHECK (arousal IS NULL OR (arousal >= -1.0 AND arousal <= 1.0)),
    CONSTRAINT chk_emotional_event_dominance
        CHECK (dominance IS NULL OR (dominance >= -1.0 AND dominance <= 1.0)),
    CONSTRAINT chk_emotional_event_intensity
        CHECK (intensity IS NULL OR (intensity >= 0.0 AND intensity <= 1.0)),
    CONSTRAINT chk_emotional_event_feedback_score
        CHECK (feedback_score IS NULL OR (feedback_score >= 1 AND feedback_score <= 5)),
    CONSTRAINT chk_emotional_event_decision
        CHECK (recommendation_decision IS NULL OR recommendation_decision IN ('ACCEPTED', 'IGNORED', 'CHOSE_OTHER'))
);

CREATE INDEX IF NOT EXISTS idx_emotional_event_user_id ON emotional_event(user_id);
CREATE INDEX IF NOT EXISTS idx_emotional_event_source ON emotional_event(source);
CREATE INDEX IF NOT EXISTS idx_emotional_event_detected_emotion ON emotional_event(detected_emotion);
CREATE INDEX IF NOT EXISTS idx_emotional_event_created_at ON emotional_event(created_at);
