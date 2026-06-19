package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.infrastructure.repository.entity.AntiScrollConfigEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AntiScrollGlobalConfigMapperTest {

    private final AntiScrollGlobalConfigMapper mapper = new AntiScrollGlobalConfigMapper();

    @Test
    void toEntity_shouldMapCorrectly() {
        AntiScrollGlobalConfig config = AntiScrollGlobalConfig.builder()
                .id(1L)
                .defaultPauseIntervalMinutes(15)
                .termsAndConditions("test terms")
                .build();

        AntiScrollConfigEntity entity = mapper.toEntity(config);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getDefaultPauseIntervalMinutes()).isEqualTo(15);
        assertThat(entity.getTermsAndConditions()).isEqualTo("test terms");
    }

    @Test
    void toDomain_shouldMapCorrectly() {
        AntiScrollConfigEntity entity = AntiScrollConfigEntity.builder()
                .id(2L)
                .defaultPauseIntervalMinutes(25)
                .termsAndConditions("domain terms")
                .build();

        AntiScrollGlobalConfig config = mapper.toDomain(entity);

        assertThat(config).isNotNull();
        assertThat(config.getId()).isEqualTo(2L);
        assertThat(config.getDefaultPauseIntervalMinutes()).isEqualTo(25);
        assertThat(config.getTermsAndConditions()).isEqualTo("domain terms");
    }

    @Test
    void toEntity_shouldReturnNull_whenNullPassed() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDomain_shouldReturnNull_whenNullPassed() {
        assertThat(mapper.toDomain(null)).isNull();
    }
}
