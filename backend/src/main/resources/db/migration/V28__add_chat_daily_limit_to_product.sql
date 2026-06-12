-- Límite diario de mensajes de chat por plan. NULL = ilimitado.
-- Solo aplica a productos de tipo PLAN; el valor de cada plan se carga manualmente.
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS chat_daily_limit INTEGER;

-- Identificar la membresía por el producto comprado (no solo por plan_code).
ALTER TABLE user_plan
    ADD COLUMN IF NOT EXISTS product_id BIGINT REFERENCES product(id);

-- Backfill: enlazar membresías existentes con el producto PLAN de su plan_code.
UPDATE user_plan up
SET product_id = (
    SELECT p.id FROM product p
    WHERE p.plan_code = up.plan_code AND p.type = 'PLAN'
    ORDER BY p.id
    LIMIT 1
)
WHERE product_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_user_plan_product_id ON user_plan(product_id);
