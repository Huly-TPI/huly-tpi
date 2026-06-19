package com.huly.backend.domain.useCase.emotionalRecommendation;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetEmotionalRecommendationsUseCaseTest {

    private ActivityRepository activityRepository;
    private EmotionalEventRepository emotionalEventRepository;
    private EmotionalRecommendationService recommendationService;
    private GetEmotionalRecommendationsUseCase useCase;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        emotionalEventRepository = mock(EmotionalEventRepository.class);
        recommendationService = mock(EmotionalRecommendationService.class);
        useCase = new GetEmotionalRecommendationsUseCase(activityRepository, emotionalEventRepository, recommendationService);
    }

    @Test
    void execute_shouldLoadActivitiesAndDelegateRankingWithoutHistory_whenUserIdIsMissing() {
        EmotionalRecommendation query = query(new Vad(-0.7, 0.8, -0.6), 0.8);
        List<Activity> activities = List.of(Activity.builder().id(1L).build());
        EmotionalRecommendationResult expected = new EmotionalRecommendationResult(List.of(), false);
        when(activityRepository.findAll()).thenReturn(activities);
        when(recommendationService.recommend(query, activities, List.of())).thenReturn(expected);

        EmotionalRecommendationResult result = useCase.execute(query);

        assertThat(result).isSameAs(expected);
        verifyNoInteractions(emotionalEventRepository);
        verify(recommendationService).recommend(query, activities, List.of());
    }

    @Test
    void execute_shouldLoadUserHistoryAndDelegateRanking_whenUserIdIsPresent() {
        EmotionalRecommendation query = new EmotionalRecommendation(7L, new Vad(-0.7, 0.8, -0.6), 0.8, "calmarme");
        List<Activity> activities = List.of(Activity.builder().id(1L).build());
        List<EmotionalEvent> history = List.of(EmotionalEvent.builder().userId(7L).build());
        EmotionalRecommendationResult expected = new EmotionalRecommendationResult(List.of(), false);
        when(activityRepository.findAll()).thenReturn(activities);
        when(emotionalEventRepository.findRecentRecommendationHistoryByUserId(7L, 20)).thenReturn(history);
        when(recommendationService.recommend(query, activities, history)).thenReturn(expected);

        EmotionalRecommendationResult result = useCase.execute(query);

        assertThat(result).isSameAs(expected);
        verify(emotionalEventRepository).findRecentRecommendationHistoryByUserId(7L, 20);
        verify(recommendationService).recommend(query, activities, history);
    }

    @Test
    void execute_shouldRejectInvalidVadBeforeLoadingActivities() {
        EmotionalRecommendation query = query(new Vad(-1.1, 0.0, 0.0), 0.5);

        assertThatThrownBy(() -> useCase.execute(query))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("valence");
        verify(activityRepository, never()).findAll();
    }

    @Test
    void execute_shouldRejectInvalidIntensityBeforeLoadingActivities() {
        EmotionalRecommendation query = query(new Vad(0.0, 0.0, 0.0), 1.1);

        assertThatThrownBy(() -> useCase.execute(query))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("intensity");
        verify(activityRepository, never()).findAll();
    }

    private EmotionalRecommendation query(Vad vad, double intensity) {
        return new EmotionalRecommendation(vad, intensity, "calmarme");
    }
}
