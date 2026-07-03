ALTER TABLE store_item
    ALTER COLUMN asset_key DROP NOT NULL,
    ADD COLUMN image_url VARCHAR(500);