package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.dailyReward.DailyRewardClaim;
import com.huly.backend.domain.model.dailyReward.DailyRewardStatus;
import com.huly.backend.domain.useCase.dailyReward.ClaimDailyRewardUseCase;
import com.huly.backend.domain.useCase.dailyReward.GetDailyRewardStatusUseCase;
import com.huly.backend.infrastructure.presentation.dto.dailyReward.ClaimDailyRewardResponse;
import com.huly.backend.infrastructure.presentation.dto.dailyReward.DailyRewardDayResponse;
import com.huly.backend.infrastructure.presentation.dto.dailyReward.DailyRewardStatusResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
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

    @GetMapping("/status")
    public ResponseEntity<DailyRewardStatusResponse> getStatus(@AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        DailyRewardStatus status = getDailyRewardStatusUseCase.execute(userId);
        return ResponseEntity.ok(toResponse(status));
    }

    @PostMapping("/claim")
    public ResponseEntity<ClaimDailyRewardResponse> claim(@AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        DailyRewardClaim claim = claimDailyRewardUseCase.execute(userId);
        return ResponseEntity.ok(new ClaimDailyRewardResponse(claim.coins(), claim.dayNumber(), claim.newStreak()));
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

    private DailyRewardStatusResponse toResponse(DailyRewardStatus status) {
        return new DailyRewardStatusResponse(
                status.days().stream()
                        .map(d -> new DailyRewardDayResponse(d.getDayNumber(), d.getCoins()))
                        .toList(),
                status.currentStreak(),
                status.completedDays(),
                status.canClaimToday(),
                status.nextDay()
        );
    }
}
