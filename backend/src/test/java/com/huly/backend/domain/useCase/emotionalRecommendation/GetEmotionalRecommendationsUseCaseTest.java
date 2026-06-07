package com.huly.backend.domain.useCase.emotionalRecommendation;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.Vad;
import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.service.EmotionalRecommendationService;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetEmotionalRecommendationsUseCaseTest {

    private ActivityRepository activityRepository;
    private EmotionalRecommendationService recommendationService;
    private GetEmotionalRecommendationsUseCase useCase;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        recommendationService = mock(EmotionalRecommendationService.class);
        useCase = new GetEmotionalRecommendationsUseCase(activityRepository, recommendationService);
    }

    @Test
    void execute_shouldLoadActivitiesAndDelegateRanking() {
        EmotionalRecommendationQuery query = query(new Vad(-0.7, 0.8, -0.6), 0.8);
        List<Activity> activities = List.of(Activity.builder().id(1L).build());
        EmotionalRecommendationResult expected = new EmotionalRecommendationResult(List.of(), false);
        when(activityRepository.findAll()).thenReturn(activities);
        when(recommendationService.recommend(query, activities)).thenReturn(expected);

        EmotionalRecommendationResult result = useCase.execute(query);

        assertThat(result).isSameAs(expected);
        verify(recommendationService).recommend(query, activities);
    }

    @Test
    void execute_shouldRejectInvalidVadBeforeLoadingActivities() {
        EmotionalRecommendationQuery query = query(new Vad(-1.1, 0.0, 0.0), 0.5);

        assertThatThrownBy(() -> useCase.execute(query))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("valence");
        verify(activityRepository, never()).findAll();
    }

    @Test
    void execute_shouldRejectInvalidIntensityBeforeLoadingActivities() {
        EmotionalRecommendationQuery query = query(new Vad(0.0, 0.0, 0.0), 1.1);

        assertThatThrownBy(() -> useCase.execute(query))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("intensity");
        verify(activityRepository, never()).findAll();
    }

    private EmotionalRecommendationQuery query(Vad vad, double intensity) {
        return new EmotionalRecommendationQuery(vad, intensity, "calmarme");
    }
}
