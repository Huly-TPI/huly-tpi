# Vector Memory

La memoria vectorial usa Spring AI `VectorStore` con PostgreSQL + pgvector sobre la tabla `vector_store`.

## Modelo y dimension

- Proveedor de chat del proyecto: Anthropic, configurado con `ANTHROPIC_API_KEY`.
- La API de Anthropic se usa para generar respuestas del chatbot, no para embeddings.
- La memoria vectorial requiere un `EmbeddingModel` de Spring AI. Si no existe un proveedor de embeddings compatible, `VECTOR_STORE_TYPE` debe quedar en `none` y se usa `NoOpVectorMemoryService`.
- Para activar pgvector, configurar `VECTOR_STORE_TYPE=pgvector` y un proveedor de embeddings compatible con Spring AI.
- Dimension configurada: `VECTOR_EMBEDDING_DIMENSION`.

La dimension configurada debe coincidir con la dimension real del modelo de embeddings. Si se cambia el modelo y cambia la dimension, no alcanza con cambiar la property: hay que crear una nueva migracion, recrear/reindexar los embeddings existentes o mantener tablas separadas por version de modelo.

La migracion actual crea `embedding VECTOR(1024)`. Si el proveedor de embeddings elegido devuelve otra dimension, se debe crear una nueva migracion antes de activar `VECTOR_STORE_TYPE=pgvector`.

## Metadata

Cada documento guarda metadata generica para filtrar por funcionalidad y usuario:

- `userId`
- `sourceType`
- `sourceId`
- `source`
- `contentType`
- `conversationId`
- `messageId`
- `createdAt`
- `deleted`
- `deletedAt`, cuando se invalida por soft delete

## Fuentes soportadas

- `CHATBOT`
- `EMOTIONAL_JOURNAL`
- `GUIDED_CLOUDS`

Cada fuente puede tener su propia politica de memoria implementando `VectorMemorySourcePolicy`.
