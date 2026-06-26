package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.useCase.badge.GetAllBadgesUseCase;
import com.huly.backend.domain.useCase.badge.GetUserBadgesUseCase;
import com.huly.backend.infrastructure.presentation.dto.badge.BadgeResponse;
import com.huly.backend.infrastructure.presentation.dto.badge.UserBadgeResponse;
import com.huly.backend.infrastructure.presentation.mapper.badge.BadgePresentationMapper;
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
    private final BadgePresentationMapper mapper;

    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> response = mapper.toBadgeResponses(getAllBadgesUseCase.execute());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserBadgeResponse>> getMyBadges(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserBadgeResponse> response = mapper.toUserBadgeResponses(
                getUserBadgesUseCase.execute(mapper.toUserBadgesRequest(Long.parseLong(userDetails.getUsername()))));
        return ResponseEntity.ok(response);
    }

}
