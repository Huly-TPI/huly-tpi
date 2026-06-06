package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.service.EmotionalRecommendationService;
import com.huly.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetEmotionalRecommendationsUseCase {

    private final ActivityRepository activityRepository;
    private final EmotionalRecommendationService recommendationService;

    public EmotionalRecommendationResult execute(EmotionalRecommendationQuery query) {
        validateRange("valence", query.valence(), -1.0, 1.0);
        validateRange("arousal", query.arousal(), -1.0, 1.0);
        validateRange("dominance", query.dominance(), -1.0, 1.0);
        validateRange("intensity", query.intensity(), 0.0, 1.0);
        if (query.confidence() != null) {
            validateRange("confidence", query.confidence(), 0.0, 1.0);
        }

        List<Activity> activities = activityRepository.findAll();
        return recommendationService.recommend(query, activities);
    }

    private void validateRange(String field, double value, double min, double max) {
        if (value < min || value > max) {
            throw new BadRequestException(field + " debe estar entre " + min + " y " + max);
        }
    }
}
