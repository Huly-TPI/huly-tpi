package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.UserPersonalitySummary;
import com.huly.backend.domain.repository.UserPersonalitySummaryRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserPersonalitySummaryEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPersonalitySummaryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPersonalitySummaryRepositoryImpl implements UserPersonalitySummaryRepository {

    private final IUserPersonalitySummaryJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<UserPersonalitySummary> findByUserId(Long userId) {
        return jpaRepository.findByAppUserId(userId)
                .map(this::toDomain)
                .or(() -> findLegacyVectorSummary(userId));
    }

    @Override
    public UserPersonalitySummary save(UserPersonalitySummary summary) {
        UserPersonalitySummaryEntity entity = jpaRepository.findByAppUserId(summary.getUserId())
                .map(existing -> updateEntity(existing, summary))
                .orElseGet(() -> toEntity(summary));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByAppUserId(userId);
    }

    private UserPersonalitySummaryEntity toEntity(UserPersonalitySummary summary) {
        AppUserEntity appUser = appUserRepository.getReferenceById(summary.getUserId());
        return UserPersonalitySummaryEntity.builder()
                .id(summary.getId())
                .appUser(appUser)
                .summary(summary.getSummary())
                .accepted(summary.getAccepted())
                .rejected(summary.getRejected())
                .generatedAt(summary.getGeneratedAt())
                .updatedAt(summary.getUpdatedAt())
                .build();
    }

    private UserPersonalitySummaryEntity updateEntity(UserPersonalitySummaryEntity entity, UserPersonalitySummary summary) {
        entity.setSummary(summary.getSummary());
        entity.setAccepted(summary.getAccepted());
        entity.setRejected(summary.getRejected());
        entity.setGeneratedAt(summary.getGeneratedAt());
        entity.setUpdatedAt(summary.getUpdatedAt());
        return entity;
    }

    private UserPersonalitySummary toDomain(UserPersonalitySummaryEntity entity) {
        return UserPersonalitySummary.builder()
                .id(entity.getId())
                .userId(entity.getAppUser().getId())
                .summary(entity.getSummary())
                .accepted(entity.getAccepted())
                .rejected(entity.getRejected())
                .generatedAt(entity.getGeneratedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Optional<UserPersonalitySummary> findLegacyVectorSummary(Long userId) {
        try {
            String sql = """
                    SELECT content
                    FROM vector_store
                    WHERE metadata ->> 'userId' = ?
                      AND COALESCE(metadata ->> 'deleted', 'false') = 'false'
                      AND metadata ->> 'contentType' = 'PERSONALITY_SUMMARY'
                    LIMIT 1
                    """;
            List<String> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("content"), userId.toString());
            return results.stream().findFirst().map(content -> parseLegacySummary(userId, content));
        } catch (Exception e) {
            log.warn("No se pudo consultar el resumen de personalidad legado para el usuario {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    private UserPersonalitySummary parseLegacySummary(Long userId, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }

        if (trimmed.startsWith("{")) {
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                return UserPersonalitySummary.builder()
                        .userId(userId)
                        .summary(node.has("summary") ? node.get("summary").asText() : content)
                        .accepted(node.has("accepted") ? node.get("accepted").asText(null) : null)
                        .rejected(node.has("rejected") ? node.get("rejected").asText(null) : null)
                        .generatedAt(Instant.EPOCH)
                        .updatedAt(Instant.EPOCH)
                        .build();
            } catch (Exception ignored) {
                // Fall back to raw summary below.
            }
        }

        return UserPersonalitySummary.builder()
                .userId(userId)
                .summary(content)
                .generatedAt(Instant.EPOCH)
                .updatedAt(Instant.EPOCH)
                .build();
    }
}
