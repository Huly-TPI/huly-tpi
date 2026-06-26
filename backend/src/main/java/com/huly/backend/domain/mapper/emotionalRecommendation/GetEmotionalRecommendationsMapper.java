package com.huly.backend.domain.mapper.emotionalRecommendation;

import com.huly.backend.domain.dto.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsRequest;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsResponse;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;

/**
 * Mapper de dominio para el caso de uso de recomendaciones emocionales.
 */
public class GetEmotionalRecommendationsMapper {

    public EmotionalRecommendation toModel(GetEmotionalRecommendationsRequest request) {
        return new EmotionalRecommendation(
                request.userId(),
                new Vad(request.valence(), request.arousal(), request.dominance()),
                request.intensity(),
                request.userGoal()
        );
    }

    public GetEmotionalRecommendationsResponse toResponse(EmotionalRecommendationResult result) {
        return new GetEmotionalRecommendationsResponse(
                result.recommendations().stream()
                        .map(this::toItem)
                        .toList(),
                result.fallbackUsed()
        );
    }

    private EmotionalRecommendationItem toItem(
            com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationItem item
    ) {
        return new EmotionalRecommendationItem(
                item.activityId(),
                item.type(),
                item.title(),
                item.description(),
                item.score(),
                item.reason()
        );
    }
}
