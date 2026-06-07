CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

INSERT INTO product (name, description, price) VALUES
    ('Pack Inicial', '100 monedas para comenzar tu experiencia', 499.00),
    ('Pack Estándar', '500 monedas con 10% de bonificación incluida', 1999.00),
    ('Pack Premium', '1500 monedas con 20% de bonificación incluida', 4999.00);

ALTER TABLE app_user ADD COLUMN coins INTEGER NOT NULL DEFAULT 0;
