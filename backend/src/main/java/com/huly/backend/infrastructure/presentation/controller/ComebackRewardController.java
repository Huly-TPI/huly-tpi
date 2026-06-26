package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusResponse;
import com.huly.backend.domain.useCase.comebackReward.ClaimComebackRewardUseCase;
import com.huly.backend.domain.useCase.comebackReward.GetComebackRewardStatusUseCase;
import com.huly.backend.infrastructure.presentation.dto.comebackReward.ClaimComebackRewardResponse;
import com.huly.backend.infrastructure.presentation.dto.comebackReward.ComebackRewardStatusResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.comebackReward.ComebackRewardPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comeback-rewards")
public class ComebackRewardController {

    private final GetComebackRewardStatusUseCase getComebackRewardStatusUseCase;
    private final ClaimComebackRewardUseCase claimComebackRewardUseCase;
    private final ComebackRewardPresentationMapper mapper;

    @GetMapping("/status")
    public ResponseEntity<ComebackRewardStatusResponse> getStatus(@AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        GetComebackRewardStatusResponse status = getComebackRewardStatusUseCase.execute(mapper.toStatusRequest(userId));
        return ResponseEntity.ok(mapper.toStatusResponse(status));
    }

    @PostMapping("/claim")
    public ResponseEntity<ClaimComebackRewardResponse> claim(@AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        return ResponseEntity.ok(mapper.toClaimResponse(
                claimComebackRewardUseCase.execute(mapper.toClaimRequest(userId))));
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
