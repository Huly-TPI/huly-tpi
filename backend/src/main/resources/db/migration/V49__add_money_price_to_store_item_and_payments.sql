ALTER TABLE store_item ADD COLUMN IF NOT EXISTS price NUMERIC(10,2);
ALTER TABLE store_item ADD COLUMN IF NOT EXISTS premium_only BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE store_item SET price = 1000.00 WHERE asset_key = 'notebook-pink';
ALTER TABLE payment_event ALTER COLUMN product_id DROP NOT NULL;
ALTER TABLE payment_event ADD COLUMN IF NOT EXISTS store_item_id BIGINT REFERENCES store_item(id);