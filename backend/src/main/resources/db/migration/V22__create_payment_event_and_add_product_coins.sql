ALTER TABLE product
    ADD COLUMN IF NOT EXISTS coins_amount INTEGER NOT NULL DEFAULT 0;

UPDATE product SET coins_amount = 100  WHERE name = 'Pack Inicial';
UPDATE product SET coins_amount = 500  WHERE name = 'Pack Estándar';
UPDATE product SET coins_amount = 1500 WHERE name = 'Pack Premium';

CREATE TABLE IF NOT EXISTS payment_event (
                                             id                BIGSERIAL PRIMARY KEY,
                                             user_id           BIGINT NOT NULL REFERENCES app_user(id),
    product_id        BIGINT NOT NULL REFERENCES product(id),
    mp_preference_id  VARCHAR(255) NOT NULL,
    mp_payment_id     BIGINT,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    coins_amount      INTEGER NOT NULL,
    error_detail      TEXT,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_payment_event_mp_preference_id
    ON payment_event(mp_preference_id);

CREATE INDEX IF NOT EXISTS idx_payment_event_mp_payment_id
    ON payment_event(mp_payment_id);
