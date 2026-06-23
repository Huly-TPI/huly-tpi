CREATE TABLE store_item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(20) NOT NULL,
    asset_key VARCHAR(100) NOT NULL UNIQUE,
    price_coins INTEGER NOT NULL
);

INSERT INTO store_item (name, description, category, asset_key, price_coins) VALUES
    ('Casa rosa', 'Pinta tu casa de un rosa suave', 'HOUSE', 'house-pink', 50),
    ('Casa celeste', 'Un celeste tranquilo para tu casa', 'HOUSE', 'house-blue', 50);