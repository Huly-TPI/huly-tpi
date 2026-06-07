package com.huly.backend.domain.useCase.emotionalRecommendation;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.Vad;
import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.service.EmotionalRecommendationService;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Retrieves and ranks activities for a validated emotional recommendation query.
 */
@RequiredArgsConstructor
public class GetEmotionalRecommendationsUseCase {

    private static final double MIN_INTENSITY = 0.0;
    private static final double MAX_INTENSITY = 1.0;

    private final ActivityRepository activityRepository;
    private final EmotionalRecommendationService recommendationService;

    /**
     * Validates the emotional state and returns all activities ordered by relevance.
     *
     * @param query emotional recommendation criteria
     * @return ordered emotional recommendations
     * @throws BadRequestException when VAD or intensity values are outside their supported ranges
     */
    public EmotionalRecommendationResult execute(EmotionalRecommendationQuery query) {
        validateVad(query.vad());
        validateRange("intensity", query.intensity(), MIN_INTENSITY, MAX_INTENSITY);

        List<Activity> activities = activityRepository.findAll();
        return recommendationService.recommend(query, activities);
    }

    private void validateVad(Vad vad) {
        if (vad == null) {
            throw new BadRequestException("VAD es obligatorio");
        }
        validateVadDimension("valence", vad.valence());
        validateVadDimension("arousal", vad.arousal());
        validateVadDimension("dominance", vad.dominance());
    }

    private void validateVadDimension(String field, double value) {
        if (!Vad.isValidDimension(value)) {
            throw new BadRequestException(
                    field + " debe estar entre " + Vad.MIN_VALUE + " y " + Vad.MAX_VALUE
            );
        }
    }

    private void validateRange(String field, double value, double min, double max) {
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new BadRequestException(field + " debe estar entre " + min + " y " + max);
        }
    }
}
