package com.huly.backend.domain.mapper.badge;

import com.huly.backend.domain.dto.badge.BadgeItem;
import com.huly.backend.domain.dto.badge.GetAllBadgesResponse;
import com.huly.backend.domain.model.badge.Badge;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de insignias.
 */
public class GetAllBadgesMapper {

    public GetAllBadgesResponse toResponse(List<Badge> badges) {
        List<BadgeItem> items = badges.stream()
                .map(this::toItem)
                .toList();
        return new GetAllBadgesResponse(items);
    }

    private BadgeItem toItem(Badge badge) {
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
