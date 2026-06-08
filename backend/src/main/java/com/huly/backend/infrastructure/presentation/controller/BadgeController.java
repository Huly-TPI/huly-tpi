package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.model.Badge;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.domain.useCase.badge.GetAllBadgesUseCase;
import com.huly.backend.domain.useCase.badge.GetUserBadgesUseCase;
import com.huly.backend.infrastructure.presentation.dto.badge.BadgeResponse;
import com.huly.backend.infrastructure.presentation.dto.badge.UserBadgeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final GetAllBadgesUseCase getAllBadgesUseCase;
    private final GetUserBadgesUseCase getUserBadgesUseCase;

    @GetMapping 
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> response = getAllBadgesUseCase.execute().stream().map(this::toBadgeResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserBadgeResponse>> getMyBadges(@AuthenticationPrincipal UserDetails userDetails) {
          List<UserBadgeResponse> response = getUserBadgesUseCase.execute(userDetails.getUsername())
                .stream()
                .map(this::toUserBadgeResponse)
                .toList();  
        return ResponseEntity.ok(response);
    }

    private BadgeResponse toBadgeResponse(Badge badge) {
        return new BadgeResponse(
                badge.getId(),
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getImageUrl(),
                badge.getCreatedAt()
        );
    }

    private UserBadgeResponse toUserBadgeResponse(UserBadge userBadge) {
        return new UserBadgeResponse(
                userBadge.getId(),
                toBadgeResponse(userBadge.getBadge()),
                userBadge.getObtainedAt()
        );
    }
    
}
