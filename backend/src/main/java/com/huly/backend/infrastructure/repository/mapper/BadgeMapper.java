package com.huly.backend.infrastructure.repository.mapper;
import com.huly.backend.domain.model.Badge;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;
import org.springframework.stereotype.Component;

@Component
public class BadgeMapper {
    public Badge toDomain(BadgeEntity entity) {
        if (entity == null) {
            return null;
        }
        return Badge.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
