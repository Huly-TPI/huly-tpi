CREATE TABLE badges( 
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    image_url VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_badges (
    id BIGSERIAL PRIMARY KEY, 
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    badge_id BIGINT NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
    obtained_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_badge UNIQUE (user_id, badge_id)
);

INSERT INTO badges (code, name, description, image_url) VALUES
    ('PRIMER_PASO',  'Primer paso',  '¡Empezaste tu camino por Huly!',           'badge_primer_paso.webp'),
    ('VALENTÍA',     'Valentía',     'Te animaste a enfrentar algo difícil.',   'badge_valentia.webp'),
    ('CONSTANCIA',   'Constancia',   'Volviste día tras días.',                    'badge_constancia.webp'),
    ('CALMA',        'Calma',        'Encontraste tu centro cuando más lo necesitabas.',         'badge_calma.webp'),
    ('APERTURA',     'Apertura',     'Te abriste a explorar algo nuevo sobre vos mismo/a.',      'badge_apertura.webp'),
    ('EXPLORADOR',   'Explorador/a', 'Probaste distintas formas de sentirte mejor.',             'badge_explorador.webp'),
    ('VOZ_PROPIA',   'Voz propia',   'Le pusiste palabras a lo que sentís.',     'badge_voz_propia.webp'),
    ('LUZ_INTERIOR', 'Luz interior', 'Sostuviste algo que te costaba.',             'badge_luz_interior.webp');