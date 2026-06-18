package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.LanternRecommendation;
import com.huly.backend.domain.useCase.lanternRecommendation.GetLanternRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.dto.lanternRecommendation.LanternRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.lanternRecommendation.LanternRecommendationResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lanterns")
@RequiredArgsConstructor
public class LanternRecommendationController {

    private final GetLanternRecommendationUseCase getLanternRecommendationUseCase;

    @PostMapping("/recommendation")
    public ResponseEntity<LanternRecommendationResponse> getRecommendation(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid LanternRecommendationRequest request
    ) {
        LanternRecommendation recommendation = getLanternRecommendationUseCase.execute(
                request.thoughts(),
                getUserId(principal)
        );
        return ResponseEntity.ok(new LanternRecommendationResponse(
                recommendation.activityType(),
                recommendation.actionId(),
                recommendation.title(),
                recommendation.description(),
                recommendation.redirectUrl()
        ));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
