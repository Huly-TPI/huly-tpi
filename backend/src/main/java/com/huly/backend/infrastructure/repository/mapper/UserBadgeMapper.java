package com.huly.backend.infrastructure.repository.mapper;
import com.huly.backend.domain.model.user.UserBadge;
import com.huly.backend.infrastructure.repository.entity.UserBadgeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserBadgeMapper {
    private final BadgeMapper badgeMapper;

    public UserBadge toDomain(UserBadgeEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserBadge.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .badge(badgeMapper.toDomain(entity.getBadge()))
                .obtainedAt(entity.getObtainedAt())
                .build();
    }
    
}
