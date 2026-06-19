package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.infrastructure.repository.entity.AntiScrollConfigEntity;
import org.springframework.stereotype.Component;

@Component
public class AntiScrollGlobalConfigMapper {

    public AntiScrollConfigEntity toEntity(AntiScrollGlobalConfig config) {
        if (config == null) return null;
        return AntiScrollConfigEntity.builder()
                .id(config.getId())
                .defaultPauseIntervalMinutes(config.getDefaultPauseIntervalMinutes())
                .termsAndConditions(config.getTermsAndConditions())
                .build();
    }

    public AntiScrollGlobalConfig toDomain(AntiScrollConfigEntity entity) {
        if (entity == null) return null;
        return AntiScrollGlobalConfig.builder()
                .id(entity.getId())
                .defaultPauseIntervalMinutes(entity.getDefaultPauseIntervalMinutes())
                .termsAndConditions(entity.getTermsAndConditions())
                .build();
    }
}
