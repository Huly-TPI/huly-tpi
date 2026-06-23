-- ============================================================
-- V1 - Creación de tabla example
-- Flyway convención: V{version}__{descripcion}.sql DOS GUIONES BAJOS
-- ============================================================

CREATE TABLE example (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL,
    description TEXT,
    active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Índice para búsquedas por nombre
CREATE INDEX idx_example_name ON example (name);