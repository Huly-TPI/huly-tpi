package com.huly.backend.infrastructure.adapter.vector;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.port.VectorMemoryPort;
import com.huly.backend.domain.service.vector.VectorMemoryPolicy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "spring.ai.vectorstore.type", havingValue = "pgvector")
public class SpringAiVectorMemoryPort implements VectorMemoryPort {

    private static final String CREATED_FROM_USER_MESSAGE = "USER_MESSAGE";
    private static final String DEFAULT_CONTENT_TYPE = "TEXT_MEMORY";
    private static final TypeReference<Map<String, Object>> METADATA_TYPE = new TypeReference<>() {
    };

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VectorMemoryPolicy policy;
    private final String tableName;
    private final Integer dimensions;

    public SpringAiVectorMemoryPort(
            EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate,
            VectorMemoryPolicy policy,
            @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String tableName,
            @Value("${spring.ai.vectorstore.pgvector.dimensions:1024}") Integer dimensions
    ) {
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
        this.policy = policy;
        this.tableName = validateTableName(tableName);
        this.dimensions = dimensions;
    }

    @Override
    public void saveMemory(SaveVectorMemoryCommand command) {
        policy.validateSaveCommand(command);
        String content = policy.normalizeContent(command.content());
        if (!policy.shouldRemember(command, content)) {
            log.info("Memoria vectorial ignorada por politica para userId={} sourceType={} contentLength={}",
                    command.userId(), command.sourceType(), content.length());
            return;
        }

        UUID id = UUID.randomUUID();
        float[] embedding = embeddingModel.embed(content);
        validateEmbeddingDimensions(embedding);

        jdbcTemplate.update("""
                        INSERT INTO %s (id, content, metadata, embedding)
                        VALUES (?, ?, ?::jsonb, ?)
                        ON CONFLICT (id) DO UPDATE
                        SET content = EXCLUDED.content,
                            metadata = EXCLUDED.metadata,
                            embedding = EXCLUDED.embedding
                        """.formatted(tableName),
                id,
                content,
                toJson(buildMetadata(command, content)),
                new PGvector(embedding)
        );

        log.info("Memoria vectorial guardada id={} userId={} sourceType={} contentType={}",
                id, command.userId(), command.sourceType(), valueOrDefault(command.contentType(), DEFAULT_CONTENT_TYPE));
    }

    @Override
    public List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query) {
        String normalizedQuery = policy.validateAndNormalizeQuery(query);
        float[] embedding = embeddingModel.embed(normalizedQuery);
        validateEmbeddingDimensions(embedding);

        Double maxDistance = 1 - query.similarityThreshold();
        String sql = """
                SELECT id::text AS id,
                       content,
                       metadata::text AS metadata,
                       embedding <=> ? AS distance
                FROM %s
                WHERE metadata ->> 'userId' = ?
                  AND metadata ->> 'sourceType' = ?
                  AND COALESCE(metadata ->> 'deleted', 'false') = 'false'
                  AND embedding <=> ? <= ?
                ORDER BY distance ASC
                LIMIT ?
                """.formatted(tableName);

        return jdbcTemplate.query(
                sql,
                this::toMemory,
                new PGvector(embedding),
                query.userId().toString(),
                query.sourceType().name(),
                new PGvector(embedding),
                maxDistance,
                query.limit()
        );
    }

    @Override
    public void deleteMemories(DeleteVectorMemoryCommand command) {
        if (command == null || command.userId() == null || command.sourceType() == null) {
            throw new IllegalArgumentException("userId and sourceType are required");
        }

        String deletedAt = Instant.now().toString();
        String sql = """
                UPDATE %s
                SET metadata = jsonb_set(
                    jsonb_set(metadata::jsonb, '{deleted}', to_jsonb('true'::text), true),
                    '{deletedAt}', to_jsonb(?::text), true
                )
                WHERE metadata::jsonb ->> 'userId' = ?
                  AND metadata::jsonb ->> 'sourceType' = ?
                  AND (? IS NULL OR metadata::jsonb ->> 'sourceId' = ?)
                """.formatted(tableName);

        jdbcTemplate.update(
                sql,
                deletedAt,
                command.userId().toString(),
                command.sourceType().name(),
                command.sourceId(),
                command.sourceId()
        );
    }

    private Map<String, Object> buildMetadata(SaveVectorMemoryCommand command, String content) {
        Map<String, Object> metadata = new HashMap<>();
        if (command.metadata() != null) {
            metadata.putAll(command.metadata());
        }

        metadata.put("userId", command.userId().toString());
        metadata.put("sourceType", command.sourceType().name());
        metadata.put("module", command.sourceType().name());
        metadata.put("sourceId", valueOrEmpty(command.sourceId()));
        metadata.put("conversationId", valueOrEmpty(command.conversationId()));
        metadata.put("messageId", valueOrEmpty(command.messageId()));
        metadata.put("source", valueOrDefault(command.source(), command.sourceType().name()));
        metadata.put("createdFrom", CREATED_FROM_USER_MESSAGE);
        metadata.put("contentType", valueOrDefault(command.contentType(), DEFAULT_CONTENT_TYPE));
        metadata.put("originalMessageLength", content.length());
        metadata.put("createdAt", Instant.now().toString());
        metadata.put("deleted", "false");
        return metadata;
    }

    private VectorMemory toMemory(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> metadata = toMetadata(rs.getString("metadata"));
        Double distance = rs.getDouble("distance");
        return new VectorMemory(
                rs.getString("id"),
                parseLong(metadata.get("userId")),
                parseSource(metadata.get("sourceType")),
                valueAsString(metadata.get("sourceId")),
                rs.getString("content"),
                metadata,
                1 - distance
        );
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    private VectorMemorySource parseSource(Object value) {
        if (value == null) {
            return null;
        }
        return VectorMemorySource.valueOf(value.toString());
    }

    private String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("[a-zA-Z0-9_]{1,64}")) {
            throw new IllegalArgumentException("Invalid vector store table name");
        }
        return tableName;
    }

    private String toJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar metadata vectorial", e);
        }
    }

    private Map<String, Object> toMetadata(String metadata) {
        try {
            return objectMapper.readValue(metadata, METADATA_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer metadata vectorial", e);
        }
    }

    private void validateEmbeddingDimensions(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            throw new IllegalStateException("El proveedor de embeddings devolvio un vector vacio");
        }
        if (dimensions != null && dimensions > 0 && embedding.length != dimensions) {
            throw new IllegalStateException("Dimension de embedding invalida. Esperada: "
                    + dimensions + ", recibida: " + embedding.length);
        }
    }
}
