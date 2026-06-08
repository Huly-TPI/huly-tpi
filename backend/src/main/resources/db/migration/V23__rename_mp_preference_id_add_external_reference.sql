ALTER TABLE payment_event
    RENAME COLUMN mp_preference_id TO external_reference;

DROP INDEX IF EXISTS idx_payment_event_mp_preference_id;
CREATE INDEX IF NOT EXISTS idx_payment_event_external_reference
    ON payment_event(external_reference);

ALTER TABLE payment_event
    ADD COLUMN IF NOT EXISTS mp_preference_id VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_payment_event_mp_preference_id
    ON payment_event(mp_preference_id);
