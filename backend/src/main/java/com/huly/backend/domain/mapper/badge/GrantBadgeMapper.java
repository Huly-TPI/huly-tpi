package com.huly.backend.domain.mapper.badge;

import com.huly.backend.domain.dto.badge.GrantBadgeResponse;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.model.user.UserBadge;

import java.time.Instant;

/**
 * Mapper de dominio para el caso de uso de otorgamiento de insignias.
 */
public class GrantBadgeMapper {

    public UserBadge toModel(Long userId, Badge badge) {
        return UserBadge.builder()
                .userId(userId)
                .badge(badge)
                .obtainedAt(Instant.now())
                .build();
    }

    public GrantBadgeResponse toResponse(boolean granted) {
        return new GrantBadgeResponse(granted);
    }
}
