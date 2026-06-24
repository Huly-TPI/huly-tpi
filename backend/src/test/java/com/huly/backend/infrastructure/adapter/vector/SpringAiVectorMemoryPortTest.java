package com.huly.backend.infrastructure.adapter.vector;

import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.service.vector.VectorMemoryPolicy;
import com.huly.backend.domain.service.vector.VectorMemoryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpringAiVectorMemoryPortTest {

    private static final Integer DIMENSIONS = 1024;

    private RecordingJdbcTemplate jdbcTemplate;
    private FakeEmbeddingModel embeddingModel;
    private SpringAiVectorMemoryPort service;

    @BeforeEach
    void setUp() {
        VectorMemoryProperties properties = new VectorMemoryProperties();
        VectorMemoryPolicy policy = new VectorMemoryPolicy(properties);

        this.jdbcTemplate = new RecordingJdbcTemplate();
        this.embeddingModel = new FakeEmbeddingModel(DIMENSIONS);
        this.service = new SpringAiVectorMemoryPort(
                embeddingModel,
                jdbcTemplate,
                policy,
                "vector_store",
                DIMENSIONS
        );
    }

    @Test
    void saveMemory_shouldInsertVectorDocumentWithMetadataAndEmbedding() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.CHATBOT,
                "conv-1",
                "USER_CHAT_MESSAGE",
                "CHAT_MESSAGE",
                "me gusta jugar a la play",
                "conv-1",
                "msg-1",
                Map.of("createdFrom", "USER_MESSAGE")
        );

        service.saveMemory(command);

        assertThat(embeddingModel.embedCalls).isEqualTo(1);
        assertThat(jdbcTemplate.lastUpdateSql).contains("INSERT INTO vector_store");
        assertThat(jdbcTemplate.lastUpdateArgs).hasSize(4);
        assertThat(jdbcTemplate.lastUpdateArgs[1]).isEqualTo("me gusta jugar a la play");
        assertThat(jdbcTemplate.lastUpdateArgs[2].toString())
                .contains("\"userId\":\"1\"")
                .contains("\"sourceType\":\"CHATBOT\"")
                .contains("\"conversationId\":\"conv-1\"")
                .contains("\"messageId\":\"msg-1\"")
                .contains("\"deleted\":\"false\"");
    }

    @Test
    void saveMemory_shouldSkipTrivialMessagesWithoutHittingDatabase() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.CHATBOT,
                "conv-1",
                "USER_CHAT_MESSAGE",
                "CHAT_MESSAGE",
                "hola",
                "conv-1",
                null,
                null
        );

        service.saveMemory(command);

        assertThat(embeddingModel.embedCalls).isZero();
        assertThat(jdbcTemplate.updateCalls).isZero();
    }

    @Test
    void saveMemory_shouldPersistShortGuidedLanternsMessageUsingGuidedLanternsThreshold() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.GUIDED_LANTERNS,
                "lantern-1",
                "GUIDED_LANTERNS_INPUT",
                "GUIDED_LANTERNS_INPUT",
                "ansiedad",
                null,
                null,
                Map.of("createdFrom", "USER_MESSAGE")
        );

        service.saveMemory(command);

        assertThat(embeddingModel.embedCalls).isEqualTo(1);
        assertThat(jdbcTemplate.updateCalls).isEqualTo(1);
        assertThat(jdbcTemplate.lastUpdateArgs[1]).isEqualTo("ansiedad");
    }

    @Test
    void findRelevantMemories_shouldQueryByUserSourceAndMapResult() {
        jdbcTemplate.searchRowId = "mem-1";
        jdbcTemplate.searchRowContent = "Al usuario le gusta la play";
        jdbcTemplate.searchRowMetadata = """
                {"userId":"1","sourceType":"CHATBOT","sourceId":"conv-1","conversationId":"conv-1","deleted":"false"}
                """;
        jdbcTemplate.searchRowDistance = 0.2d;

        List<VectorMemory> result = service.findRelevantMemories(new SearchVectorMemoryQuery(
                1L,
                VectorMemorySource.CHATBOT,
                "me gusta jugar a la play",
                5,
                0.65
        ));

        assertThat(embeddingModel.embedCalls).isEqualTo(1);
        assertThat(jdbcTemplate.lastQuerySql)
                .contains("WHERE metadata ->> 'userId' = ?")
                .contains("metadata ->> 'sourceType' = ?")
                .contains("COALESCE(metadata ->> 'deleted', 'false') = 'false'")
                .contains("embedding <=> ? <= ?")
                .contains("ORDER BY distance ASC")
                .contains("LIMIT ?")
                .doesNotContain("conversationId");
        assertThat(jdbcTemplate.lastQueryArgs).hasSize(6);
        assertThat(result).hasSize(1);

        VectorMemory memory = result.get(0);
        assertThat(memory.id()).isEqualTo("mem-1");
        assertThat(memory.userId()).isEqualTo(1L);
        assertThat(memory.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(memory.content()).isEqualTo("Al usuario le gusta la play");
        assertThat(memory.score()).isEqualTo(0.8d);
    }

    @Test
    void deleteMemories_shouldSoftDeleteByUserSourceAndSourceId() {
        service.deleteMemories(new DeleteVectorMemoryCommand(1L, VectorMemorySource.CHATBOT, "conv-1"));

        assertThat(jdbcTemplate.lastUpdateSql)
                .contains("jsonb_set")
                .contains("deletedAt")
                .contains("metadata::jsonb ->> 'userId'")
                .contains("metadata::jsonb ->> 'sourceType'");
        assertThat(Arrays.asList(jdbcTemplate.lastUpdateArgs))
                .contains("1", "CHATBOT", "conv-1");
    }

    private static final class FakeEmbeddingModel implements EmbeddingModel {

        private final int dimensions;
        private final float[] embedding;
        private int embedCalls;

        private FakeEmbeddingModel(Integer dimensions) {
            this.dimensions = dimensions;
            this.embedding = new float[dimensions];
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            throw new UnsupportedOperationException("No se usa en este test");
        }

        @Override
        public float[] embed(Document document) {
            return embed(document.getText());
        }

        @Override
        public float[] embed(String text) {
            this.embedCalls++;
            return embedding;
        }

        @Override
        public int dimensions() {
            return dimensions;
        }
    }

    private static final class RecordingJdbcTemplate extends JdbcTemplate {

        private String lastUpdateSql;
        private Object[] lastUpdateArgs = new Object[0];
        private int updateCalls;
        private String lastQuerySql;
        private Object[] lastQueryArgs = new Object[0];
        private String searchRowId;
        private String searchRowContent;
        private String searchRowMetadata;
        private Double searchRowDistance;

        @Override
        public int update(String sql, Object... args) {
            this.lastUpdateSql = sql;
            this.lastUpdateArgs = args;
            this.updateCalls++;
            return 1;
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
            this.lastQuerySql = sql;
            this.lastQueryArgs = args;

            if (searchRowId == null) {
                return List.of();
            }

            try {
                ResultSet rs = fakeResultSet();
                return List.of(rowMapper.mapRow(rs, 0));
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo simular el ResultSet", e);
            }
        }

        private ResultSet fakeResultSet() {
            InvocationHandler handler = (Object proxy, Method method, Object[] args) -> {
                String name = method.getName();
                if ("getString".equals(name)) {
                    String column = String.valueOf(args[0]);
                    return switch (column) {
                        case "id" -> searchRowId;
                        case "content" -> searchRowContent;
                        case "metadata" -> searchRowMetadata;
                        default -> null;
                    };
                }
                if ("getDouble".equals(name)) {
                    return searchRowDistance != null ? searchRowDistance : 0.0d;
                }
                if ("wasNull".equals(name)) {
                    return false;
                }
                Class<?> returnType = method.getReturnType();
                if (returnType.equals(boolean.class)) {
                    return false;
                }
                if (returnType.equals(int.class)) {
                    return 0;
                }
                if (returnType.equals(long.class)) {
                    return 0L;
                }
                if (returnType.equals(float.class)) {
                    return 0f;
                }
                if (returnType.equals(double.class)) {
                    return 0d;
                }
                if (returnType.equals(short.class)) {
                    return (short) 0;
                }
                if (returnType.equals(byte.class)) {
                    return (byte) 0;
                }
                return null;
            };

            return (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class<?>[]{ResultSet.class},
                    handler
            );
        }
    }
}
