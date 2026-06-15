CREATE TABLE store_item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(20) NOT NULL,
    asset_key VARCHAR(100) NOT NULL UNIQUE,
    price_coins INTEGER NOT NULL
);

INSERT INTO store_item (name, description, category, asset_key, price_coins) VALUES
    ('Casa rosa', 'Pinta tu casa de un rosa suave', 'HOUSE', 'casa-rosa', 50),
    ('Casa celeste', 'Un celeste tranquilo para tu casa', 'HOUSE', 'casa-celeste', 50),
    ('Maceta lila', 'Una maceta en tono lila', 'POT', 'maceta-lila', 30),
    ('Maceta terracota', 'Maceta de barro terracota', 'POT', 'maceta-terracota', 30);