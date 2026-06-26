package com.huly.backend.domain.useCase.emotionalRecommendation;

import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsRequest;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsResponse;
import com.huly.backend.domain.mapper.emotionalRecommendation.GetEmotionalRecommendationsMapper;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.model.enums.ActivityType;
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
        useCase = new GetEmotionalRecommendationsUseCase(
                activityRepository, emotionalEventRepository, recommendationService,
                new GetEmotionalRecommendationsMapper());
    }

    @Test
    void execute_shouldLoadActivitiesAndDelegateRankingWithoutHistory_whenUserIdIsMissing() {
        GetEmotionalRecommendationsRequest request = request(null, new Vad(-0.7, 0.8, -0.6), 0.8);
        EmotionalRecommendation query = query(null, new Vad(-0.7, 0.8, -0.6), 0.8);
        List<Activity> activities = List.of(Activity.builder().id(1L).build());
        EmotionalRecommendationResult expected = new EmotionalRecommendationResult(
                List.of(new EmotionalRecommendationItem(
                        1L, ActivityType.RESPIRACION, "Respiracion", "Descripcion", 0.9, "razon")),
                false);
        when(activityRepository.findAll()).thenReturn(activities);
        when(recommendationService.recommend(query, activities, List.of())).thenReturn(expected);

        GetEmotionalRecommendationsResponse result = useCase.execute(request);

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.recommendations()).hasSize(1);
        assertThat(result.recommendations().get(0).activityId()).isEqualTo(1L);
        assertThat(result.recommendations().get(0).type()).isEqualTo(ActivityType.RESPIRACION);
        verifyNoInteractions(emotionalEventRepository);
        verify(recommendationService).recommend(query, activities, List.of());
    }

    @Test
    void execute_shouldLoadUserHistoryAndDelegateRanking_whenUserIdIsPresent() {
        GetEmotionalRecommendationsRequest request = new GetEmotionalRecommendationsRequest(
                7L, -0.7, 0.8, -0.6, 0.8, "calmarme");
        EmotionalRecommendation query = new EmotionalRecommendation(7L, new Vad(-0.7, 0.8, -0.6), 0.8, "calmarme");
        List<Activity> activities = List.of(Activity.builder().id(1L).build());
        List<EmotionalEvent> history = List.of(EmotionalEvent.builder().userId(7L).build());
        EmotionalRecommendationResult expected = new EmotionalRecommendationResult(List.of(), false);
        when(activityRepository.findAll()).thenReturn(activities);
        when(emotionalEventRepository.findRecentRecommendationHistoryByUserId(7L, 20)).thenReturn(history);
        when(recommendationService.recommend(query, activities, history)).thenReturn(expected);

        GetEmotionalRecommendationsResponse result = useCase.execute(request);

        assertThat(result.recommendations()).isEmpty();
        assertThat(result.fallbackUsed()).isFalse();
        verify(emotionalEventRepository).findRecentRecommendationHistoryByUserId(7L, 20);
        verify(recommendationService).recommend(query, activities, history);
    }

    @Test
    void execute_shouldRejectInvalidVadBeforeLoadingActivities() {
        GetEmotionalRecommendationsRequest request = request(null, new Vad(-1.1, 0.0, 0.0), 0.5);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("valence");
        verify(activityRepository, never()).findAll();
    }

    @Test
    void execute_shouldRejectInvalidIntensityBeforeLoadingActivities() {
        GetEmotionalRecommendationsRequest request = request(null, new Vad(0.0, 0.0, 0.0), 1.1);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("intensity");
        verify(activityRepository, never()).findAll();
    }

    private GetEmotionalRecommendationsRequest request(Long userId, Vad vad, double intensity) {
        return new GetEmotionalRecommendationsRequest(
                userId, vad.valence(), vad.arousal(), vad.dominance(), intensity, "calmarme");
    }

    private EmotionalRecommendation query(Long userId, Vad vad, double intensity) {
        return new EmotionalRecommendation(userId, vad, intensity, "calmarme");
    }
}
