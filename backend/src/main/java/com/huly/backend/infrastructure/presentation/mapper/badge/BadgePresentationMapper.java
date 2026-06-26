package com.huly.backend.infrastructure.presentation.mapper.badge;

import com.huly.backend.domain.dto.badge.BadgeItem;
import com.huly.backend.domain.dto.badge.GetAllBadgesResponse;
import com.huly.backend.domain.dto.badge.GetUserBadgesRequest;
import com.huly.backend.domain.dto.badge.GetUserBadgesResponse;
import com.huly.backend.domain.dto.badge.UserBadgeItem;
import com.huly.backend.infrastructure.presentation.dto.badge.BadgeResponse;
import com.huly.backend.infrastructure.presentation.dto.badge.UserBadgeResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de insignias:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class BadgePresentationMapper {

    public GetUserBadgesRequest toUserBadgesRequest(Long userId) {
        return new GetUserBadgesRequest(userId);
    }

    public List<BadgeResponse> toBadgeResponses(GetAllBadgesResponse response) {
        return response.badges().stream()
                .map(this::toBadgeResponse)
                .toList();
    }

    public List<UserBadgeResponse> toUserBadgeResponses(GetUserBadgesResponse response) {
        return response.badges().stream()
                .map(this::toUserBadgeResponse)
                .toList();
    }

    private BadgeResponse toBadgeResponse(BadgeItem item) {
        return new BadgeResponse(
                item.id(),
                item.code(),
                item.name(),
                item.description(),
                item.imageUrl(),
                item.createdAt()
        );
    }

    private UserBadgeResponse toUserBadgeResponse(UserBadgeItem item) {
        return new UserBadgeResponse(
                item.id(),
                toBadgeResponse(item.badge()),
                item.obtainedAt()
        );
    }
}
