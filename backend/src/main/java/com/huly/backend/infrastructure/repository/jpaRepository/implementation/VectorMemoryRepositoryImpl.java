package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import com.huly.backend.domain.repository.VectorMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VectorMemoryRepositoryImpl implements VectorMemoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<VectorMemoryEntry> findMemoriesByUserIdExcludingSummary(Long userId) {
        try {
            String sql = "SELECT id::text AS id, content, metadata::text AS metadata FROM vector_store WHERE metadata ->> 'userId' = ? AND COALESCE(metadata ->> 'deleted', 'false') = 'false' AND (metadata ->> 'contentType' IS NULL OR metadata ->> 'contentType' != 'PERSONALITY_SUMMARY')";
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                String id = rs.getString("id");
                String content = rs.getString("content");
                String metadataStr = rs.getString("metadata");
                Map<String, Object> metadata = new HashMap<>();
                try {
                    metadata = objectMapper.readValue(metadataStr, new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    // Ignore parsing error
                }

                String sourceType = metadata.containsKey("sourceType") ? metadata.get("sourceType").toString() : "UNKNOWN";
                String contentType = metadata.containsKey("contentType") ? metadata.get("contentType").toString() : "TEXT_MEMORY";
                String createdAtStr = metadata.containsKey("createdAt") ? metadata.get("createdAt").toString() : null;

                return new VectorMemoryEntry(id, content, sourceType, contentType, createdAtStr);
            }, userId.toString());
        } catch (Exception e) {
            log.warn("No se pudieron consultar las memorias vectoriales para el usuario {} (puede que pgvector este desactivado): {}", userId, e.getMessage());
            return List.of();
        }
    }

}
