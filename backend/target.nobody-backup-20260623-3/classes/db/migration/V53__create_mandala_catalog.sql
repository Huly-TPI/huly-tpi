CREATE TABLE IF NOT EXISTS mandala (
    id VARCHAR(100) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    asset_key VARCHAR(100) NOT NULL UNIQUE,
    display_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    access_type VARCHAR(20) NOT NULL,
    price_coins INTEGER,
    CONSTRAINT ck_mandala_access_type CHECK (access_type IN ('FREE', 'PURCHASABLE', 'SUBSCRIPTION')),
    CONSTRAINT ck_mandala_price_for_purchasable CHECK (
        (access_type = 'PURCHASABLE' AND price_coins IS NOT NULL AND price_coins > 0)
        OR (access_type <> 'PURCHASABLE')
    )
);

CREATE TABLE IF NOT EXISTS mandala_plan_entitlement (
    id BIGSERIAL PRIMARY KEY,
    plan_code VARCHAR(50) NOT NULL,
    mandala_id VARCHAR(100) NOT NULL REFERENCES mandala(id) ON DELETE CASCADE,
    CONSTRAINT uq_mandala_plan_entitlement UNIQUE (plan_code, mandala_id)
);

CREATE INDEX IF NOT EXISTS idx_mandala_plan_entitlement_plan_code ON mandala_plan_entitlement(plan_code);

INSERT INTO mandala (id, title, description, asset_key, display_order, active, access_type, price_coins) VALUES
    ('mandala-01', 'Mandala 01', 'Trazos circulares para pintar con calma.', 'mandala-01', 1, TRUE, 'FREE', NULL),
    ('mandala-02', 'Mandala 02', 'Formas amplias para explorar colores.', 'mandala-02', 2, TRUE, 'FREE', NULL),
    ('mandala-03', 'Mandala 03', 'Patron geometrico inspirado en flores.', 'mandala-03', 3, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-04', 'Mandala 04', 'Detalle fino para una experiencia concentrada.', 'mandala-04', 4, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-05', 'Mandala 05', 'Lineas organicas para pintar por secciones.', 'mandala-05', 5, TRUE, 'PURCHASABLE', 100)
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    asset_key = EXCLUDED.asset_key,
    display_order = EXCLUDED.display_order,
    active = EXCLUDED.active,
    access_type = EXCLUDED.access_type,
    price_coins = EXCLUDED.price_coins;

INSERT INTO mandala_plan_entitlement (plan_code, mandala_id) VALUES
    ('BASIC', 'mandala-03'),
    ('BASIC', 'mandala-04'),
    ('PREMIUM', 'mandala-03'),
    ('PREMIUM', 'mandala-04')
ON CONFLICT (plan_code, mandala_id) DO NOTHING;
