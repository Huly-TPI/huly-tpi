CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    content TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    embedding VECTOR(1024) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_vector_store_embedding_hnsw
    ON vector_store
    USING HNSW (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS idx_vector_store_metadata_gin
    ON vector_store
    USING GIN (metadata);

CREATE INDEX IF NOT EXISTS idx_vector_store_user_source_deleted
    ON vector_store (
        (metadata ->> 'userId'),
        (metadata ->> 'sourceType'),
        (metadata ->> 'deleted')
    );

CREATE INDEX IF NOT EXISTS idx_vector_store_source_id
    ON vector_store ((metadata ->> 'sourceId'));
