package com.huly.backend.infrastructure.presentation.mapper.emotionalRecommendation;

import com.huly.backend.domain.dto.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsRequest;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsResponse;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationItemResponse;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de recomendaciones emocionales:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class EmotionalRecommendationPresentationMapper {

    public GetEmotionalRecommendationsRequest toGetRequest(Long userId, EmotionalRecommendationRequest request) {
        return new GetEmotionalRecommendationsRequest(
                userId,
                request.valence(),
                request.arousal(),
                request.dominance(),
                request.intensity(),
                request.userGoal()
        );
    }

    public EmotionalRecommendationResponse toRecommendationResponse(GetEmotionalRecommendationsResponse response) {
        return new EmotionalRecommendationResponse(
                response.recommendations().stream()
                        .map(this::toItemResponse)
                        .toList(),
                response.fallbackUsed()
        );
    }

    private EmotionalRecommendationItemResponse toItemResponse(EmotionalRecommendationItem item) {
        return new EmotionalRecommendationItemResponse(
                item.activityId(),
                item.type(),
                item.title(),
                item.description(),
                item.score(),
                item.reason()
        );
    }
}
