DELETE FROM store_item WHERE asset_key = 'mandala-05';

INSERT INTO mandala (id, title, description, asset_key, display_order, active, access_type, price_coins) VALUES
    ('mandala-06', 'Mandala 06', 'Diseno armonico para pintar con calma.', 'mandala-06', 6, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-07', 'Mandala 07', 'Formas repetitivas para explorar combinaciones.', 'mandala-07', 7, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-08', 'Mandala 08', 'Patron circular con detalles equilibrados.', 'mandala-08', 8, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-09', 'Mandala 09', 'Trazos amplios para una pintura relajada.', 'mandala-09', 9, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-10', 'Mandala 10', 'Composicion radial para colorear por capas.', 'mandala-10', 10, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-11', 'Mandala 11', 'Detalle ornamental para pintar con precision.', 'mandala-11', 11, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-12', 'Mandala 12', 'Figuras suaves para combinar tonos.', 'mandala-12', 12, TRUE, 'SUBSCRIPTION', NULL),
    ('mandala-13', 'Mandala 13', 'Mandala desbloqueable con monedas.', 'mandala-13', 13, TRUE, 'PURCHASABLE', 100),
    ('mandala-14', 'Mandala 14', 'Mandala desbloqueable con monedas.', 'mandala-14', 14, TRUE, 'PURCHASABLE', 100),
    ('mandala-15', 'Mandala 15', 'Mandala desbloqueable con monedas.', 'mandala-15', 15, TRUE, 'PURCHASABLE', 100),
    ('mandala-16', 'Mandala 16', 'Mandala desbloqueable con monedas.', 'mandala-16', 16, TRUE, 'PURCHASABLE', 100),
    ('mandala-17', 'Mandala 17', 'Mandala desbloqueable con monedas.', 'mandala-17', 17, TRUE, 'PURCHASABLE', 100),
    ('mandala-18', 'Mandala 18', 'Mandala desbloqueable con monedas.', 'mandala-18', 18, TRUE, 'PURCHASABLE', 100),
    ('mandala-19', 'Mandala 19', 'Mandala desbloqueable con monedas.', 'mandala-19', 19, TRUE, 'PURCHASABLE', 100),
    ('mandala-20', 'Mandala 20', 'Mandala desbloqueable con monedas.', 'mandala-20', 20, TRUE, 'PURCHASABLE', 100),
    ('mandala-21', 'Mandala 21', 'Mandala desbloqueable con monedas.', 'mandala-21', 21, TRUE, 'PURCHASABLE', 100)
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    asset_key = EXCLUDED.asset_key,
    display_order = EXCLUDED.display_order,
    active = EXCLUDED.active,
    access_type = EXCLUDED.access_type,
    price_coins = EXCLUDED.price_coins;

UPDATE mandala
SET access_type = 'SUBSCRIPTION',
    price_coins = NULL
WHERE id = 'mandala-05';

INSERT INTO mandala_plan_entitlement (plan_code, mandala_id)
SELECT plan_code, mandala_id
FROM (VALUES ('BASIC'), ('PREMIUM')) AS plans(plan_code)
CROSS JOIN (VALUES
    ('mandala-03'),
    ('mandala-04'),
    ('mandala-05'),
    ('mandala-06'),
    ('mandala-07'),
    ('mandala-08'),
    ('mandala-09'),
    ('mandala-10'),
    ('mandala-11'),
    ('mandala-12')
) AS mandalas(mandala_id)
ON CONFLICT (plan_code, mandala_id) DO NOTHING;

INSERT INTO store_item (name, description, category, asset_key, price_coins) VALUES
    ('Mandala 13', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-13', 100),
    ('Mandala 14', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-14', 100),
    ('Mandala 15', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-15', 100),
    ('Mandala 16', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-16', 100),
    ('Mandala 17', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-17', 100),
    ('Mandala 18', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-18', 100),
    ('Mandala 19', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-19', 100),
    ('Mandala 20', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-20', 100),
    ('Mandala 21', 'Mandala desbloqueable con monedas.', 'MANDALA', 'mandala-21', 100)
ON CONFLICT (asset_key) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    price_coins = EXCLUDED.price_coins;
