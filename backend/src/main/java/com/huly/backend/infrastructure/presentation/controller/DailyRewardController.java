package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.useCase.dailyReward.ClaimDailyRewardUseCase;
import com.huly.backend.domain.useCase.dailyReward.GetDailyRewardStatusUseCase;
import com.huly.backend.infrastructure.presentation.dto.dailyReward.ClaimDailyRewardResponse;
import com.huly.backend.infrastructure.presentation.dto.dailyReward.DailyRewardStatusResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.dailyReward.DailyRewardPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/daily-rewards")
public class DailyRewardController {

    private final ClaimDailyRewardUseCase claimDailyRewardUseCase;
    private final GetDailyRewardStatusUseCase getDailyRewardStatusUseCase;
    private final DailyRewardPresentationMapper mapper;

    @GetMapping("/status")
    public ResponseEntity<DailyRewardStatusResponse> getStatus(@AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        GetDailyRewardStatusResponse status = getDailyRewardStatusUseCase.execute(mapper.toStatusRequest(userId));
        return ResponseEntity.ok(mapper.toStatusResponse(status));
    }

    @PostMapping("/claim")
    public ResponseEntity<ClaimDailyRewardResponse> claim(@AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        return ResponseEntity.ok(mapper.toClaimResponse(
                claimDailyRewardUseCase.execute(mapper.toClaimRequest(userId))));
    }

    private Long currentUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        try {
            return Long.valueOf(principal.getUsername());
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Not authenticated");
        }
    }
}
