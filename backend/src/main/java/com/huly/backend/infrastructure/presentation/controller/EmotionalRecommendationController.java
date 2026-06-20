package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationResponse;
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
@RequiredArgsConstructor
@RequestMapping("/api/emotional-recommendations")
public class EmotionalRecommendationController {

    private final GetEmotionalRecommendationsUseCase getRecommendationsUseCase;

    @PostMapping
    public ResponseEntity<EmotionalRecommendationResponse> recommend(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody EmotionalRecommendationRequest request
    ) {
        EmotionalRecommendationResult result = getRecommendationsUseCase.execute(toQuery(request, principal));
        return ResponseEntity.ok(EmotionalRecommendationResponse.from(result));
    }

    private EmotionalRecommendation toQuery(
            EmotionalRecommendationRequest request,
            UserDetails principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return new EmotionalRecommendation(
                Long.parseLong(principal.getUsername()),
                new Vad(request.valence(), request.arousal(), request.dominance()),
                request.intensity(),
                request.userGoal()
        );
    }
}
