CREATE TABLE user_store_item (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    store_item_id BIGINT NOT NULL REFERENCES store_item(id) ON DELETE CASCADE,
    equipped BOOLEAN NOT NULL DEFAULT FALSE,
    acquired_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_store_item UNIQUE (user_id, store_item_id)
);