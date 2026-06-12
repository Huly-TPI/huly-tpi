-- Discriminador de tipo de producto: COIN_PACK (packs de monedas) vs PLAN (suscripción)
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS type VARCHAR(20) NOT NULL DEFAULT 'COIN_PACK';

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS plan_code VARCHAR(50);

-- Snapshot del tipo al momento de la compra, para que el webhook sepa qué cumplir
ALTER TABLE payment_event
    ADD COLUMN IF NOT EXISTS product_type VARCHAR(20) NOT NULL DEFAULT 'COIN_PACK';

-- Membresía exclusiva: una sola fila por usuario (un único plan activo a la vez).
-- Renovar el mismo plan extiende expires_at; cambiar de plan sobrescribe plan_code.
CREATE TABLE IF NOT EXISTS user_plan (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES app_user(id),
    plan_code   VARCHAR(50) NOT NULL,
    granted_at  TIMESTAMPTZ NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_plan_user UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_plan_user_id ON user_plan(user_id);

-- Plan de ejemplo, vendible vía Mercado Pago reutilizando el flujo existente (vigencia mensual)
INSERT INTO product (name, description, price, coins_amount, type, plan_code) VALUES
    ('Plan Premium', 'Acceso premium por 30 días', 9999.00, 0, 'PLAN', 'PREMIUM');
