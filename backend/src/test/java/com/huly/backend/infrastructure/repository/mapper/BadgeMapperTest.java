package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.Badge;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BadgeMapperTest {

    private BadgeMapper badgeMapper;

    @BeforeEach
    void setUp() {
        badgeMapper = new BadgeMapper();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        Instant now = Instant.now();
        BadgeEntity entity = BadgeEntity.builder()
                .id(1L)
                .code("PRIMER_PASO")
                .name("Primer paso")
                .description("Empezaste tu camino.")
                .imageUrl("badge_primer_paso.webp")
                .createdAt(now)
                .build();

        Badge result = badgeMapper.toDomain(entity);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("PRIMER_PASO");
        assertThat(result.getName()).isEqualTo("Primer paso");
        assertThat(result.getDescription()).isEqualTo("Empezaste tu camino.");
        assertThat(result.getImageUrl()).isEqualTo("badge_primer_paso.webp");
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldHandleNullFieldsAndImageUrl() {
        BadgeEntity entity = BadgeEntity.builder()
                .id(2L)
                .code("VALENTÍA")
                .name("Valentía")
                .build();

        Badge result = badgeMapper.toDomain(entity);
        assertThat(result.getDescription()).isNull();
        assertThat(result.getImageUrl()).isNull();
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertThat(badgeMapper.toDomain(null)).isNull();
    }

}
