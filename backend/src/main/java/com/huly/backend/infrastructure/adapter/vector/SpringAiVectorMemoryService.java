package com.huly.backend.infrastructure.adapter.vector;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.provider.VectorMemoryService;
import com.huly.backend.domain.service.vector.VectorMemoryPolicy;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
@ConditionalOnBean(VectorStore.class)
public class SpringAiVectorMemoryService implements VectorMemoryService {

    private static final String CREATED_FROM_USER_MESSAGE = "USER_MESSAGE";
    private static final String DEFAULT_CONTENT_TYPE = "TEXT_MEMORY";

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final VectorMemoryPolicy policy;
    private final String tableName;

    public SpringAiVectorMemoryService(
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate,
            VectorMemoryPolicy policy,
            @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String tableName
    ) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.policy = policy;
        this.tableName = validateTableName(tableName);
    }

    @Override
    public void saveMemory(SaveVectorMemoryCommand command) {
        policy.validateSaveCommand(command);
        String content = policy.normalizeContent(command.content());
        if (!policy.shouldRemember(command, content)) {
            return;
        }

        vectorStore.add(List.of(new Document(content, buildMetadata(command, content))));
    }

    @Override
    public List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query) {
        String normalizedQuery = policy.validateAndNormalizeQuery(query);

        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(normalizedQuery)
                .topK(query.limit())
                .similarityThreshold(query.similarityThreshold())
                .filterExpression(buildFilterExpression(query.userId(), query.sourceType()))
                .build());

        return documents.stream()
                .map(this::toMemory)
                .toList();
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

    private VectorMemory toMemory(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return new VectorMemory(
                document.getId(),
                parseLong(metadata.get("userId")),
                parseSource(metadata.get("sourceType")),
                valueAsString(metadata.get("sourceId")),
                document.getText(),
                metadata,
                document.getScore()
        );
    }

    private String buildFilterExpression(Long userId, VectorMemorySource sourceType) {
        return "userId == '%s' && sourceType == '%s' && deleted == 'false'"
                .formatted(escape(userId.toString()), sourceType.name());
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

    private String escape(String value) {
        return value.replace("'", "\\'");
    }

    private String validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("[a-zA-Z0-9_]{1,64}")) {
            throw new IllegalArgumentException("Invalid vector store table name");
        }
        return tableName;
    }
}
