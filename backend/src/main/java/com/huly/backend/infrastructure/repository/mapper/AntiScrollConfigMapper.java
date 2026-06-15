package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.extension.AntiScrollConfig;
import com.huly.backend.infrastructure.repository.entity.AntiScrollConfigEntity;
import org.springframework.stereotype.Component;

@Component
public class AntiScrollConfigMapper {

    public AntiScrollConfigEntity toEntity(AntiScrollConfig config) {
        if (config == null) return null;
        return AntiScrollConfigEntity.builder()
                .id(config.getId())
                .defaultPauseIntervalMinutes(config.getDefaultPauseIntervalMinutes())
                .termsAndConditions(config.getTermsAndConditions())
                .build();
    }

    public AntiScrollConfig toDomain(AntiScrollConfigEntity entity) {
        if (entity == null) return null;
        return AntiScrollConfig.builder()
                .id(entity.getId())
                .defaultPauseIntervalMinutes(entity.getDefaultPauseIntervalMinutes())
                .termsAndConditions(entity.getTermsAndConditions())
                .build();
    }
}
