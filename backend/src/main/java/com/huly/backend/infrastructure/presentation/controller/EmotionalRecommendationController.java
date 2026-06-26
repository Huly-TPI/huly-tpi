package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.mapper.emotionalRecommendation.EmotionalRecommendationPresentationMapper;
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
@RequiredArgsConstructor
@RequestMapping("/api/emotional-recommendations")
public class EmotionalRecommendationController {

    private final GetEmotionalRecommendationsUseCase getRecommendationsUseCase;
    private final EmotionalRecommendationPresentationMapper emotionalRecommendationPresentationMapper;

    @PostMapping
    public ResponseEntity<EmotionalRecommendationResponse> recommend(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody EmotionalRecommendationRequest request
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        Long userId = Long.parseLong(principal.getUsername());
        return ResponseEntity.ok(
                emotionalRecommendationPresentationMapper.toRecommendationResponse(
                        getRecommendationsUseCase.execute(
                                emotionalRecommendationPresentationMapper.toGetRequest(userId, request))));
    }
}
