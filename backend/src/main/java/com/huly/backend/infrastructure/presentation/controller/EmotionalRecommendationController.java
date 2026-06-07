package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.Vad;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @Valid @RequestBody EmotionalRecommendationRequest request
    ) {
        EmotionalRecommendationResult result = getRecommendationsUseCase.execute(toQuery(request));
        return ResponseEntity.ok(EmotionalRecommendationResponse.from(result));
    }

    private EmotionalRecommendationQuery toQuery(EmotionalRecommendationRequest request) {
        return new EmotionalRecommendationQuery(
                request.userId(),
                new Vad(request.valence(), request.arousal(), request.dominance()),
                request.intensity(),
                request.userGoal()
        );
    }
}
