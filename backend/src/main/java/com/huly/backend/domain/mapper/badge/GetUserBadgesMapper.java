package com.huly.backend.domain.mapper.badge;

import com.huly.backend.domain.dto.badge.BadgeItem;
import com.huly.backend.domain.dto.badge.GetUserBadgesResponse;
import com.huly.backend.domain.dto.badge.UserBadgeItem;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.model.user.UserBadge;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de insignias del usuario.
 */
public class GetUserBadgesMapper {

    public GetUserBadgesResponse toResponse(List<UserBadge> userBadges) {
        List<UserBadgeItem> items = userBadges.stream()
                .map(this::toItem)
                .toList();
        return new GetUserBadgesResponse(items);
    }

    private UserBadgeItem toItem(UserBadge userBadge) {
        return new UserBadgeItem(
                userBadge.getId(),
                toBadgeItem(userBadge.getBadge()),
                userBadge.getObtainedAt()
        );
    }

    private BadgeItem toBadgeItem(Badge badge) {
        return new BadgeItem(
                badge.getId(),
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getImageUrl(),
                badge.getCreatedAt()
        );
    }
}
