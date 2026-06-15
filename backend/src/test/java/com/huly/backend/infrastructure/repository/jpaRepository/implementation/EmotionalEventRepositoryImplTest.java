package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionalEventEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IEmotionalEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmotionalEventRepositoryImplTest {

    @Mock
    private IEmotionalEventJpaRepository emotionalEventJpaRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private IActivityJpaRepository activityJpaRepository;

    @InjectMocks
    private EmotionalEventRepositoryImpl repository;

    @Test
    void save_shouldMapDomainToEntityAndBack() {
        when(appUserRepository.getReferenceById(1L)).thenReturn(user(1L));
        when(activityJpaRepository.getReferenceById(2L)).thenReturn(activity(2L));
        when(emotionalEventJpaRepository.save(any())).thenReturn(entity());
        ArgumentCaptor<EmotionalEventEntity> captor = ArgumentCaptor.forClass(EmotionalEventEntity.class);

        EmotionalEvent result = repository.save(domain());

        verify(emotionalEventJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getRecommendedActivity().getId()).isEqualTo(2L);
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getRecommendationDecision()).isEqualTo(RecommendationDecision.ACCEPTED);
    }

    @Test
    void findById_shouldReturnMappedDomainWhenFound() {
        when(emotionalEventJpaRepository.findById(10L)).thenReturn(Optional.of(entity()));

        Optional<EmotionalEvent> result = repository.findById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        assertThat(result.get().getRecommendedActivityId()).isEqualTo(2L);
    }

    @Test
    void findById_shouldReturnEmptyWhenMissing() {
        when(emotionalEventJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(repository.findById(99L)).isEmpty();
    }

    @Test
    void findRecentRecommendationHistoryByUserId_shouldReturnMappedRecentEvents() {
        when(emotionalEventJpaRepository.findRecommendationHistoryByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(entity()));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        List<EmotionalEvent> result = repository.findRecentRecommendationHistoryByUserId(1L, 20);

        verify(emotionalEventJpaRepository).findRecommendationHistoryByUserId(eq(1L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getRecommendedActivityId()).isEqualTo(2L);
        assertThat(result.get(0).getFeedbackScore()).isEqualTo(4);
    }

    @Test
    void findRecentRecommendationHistoryByUserId_shouldReturnEmptyWhenUserIdIsMissing() {
        assertThat(repository.findRecentRecommendationHistoryByUserId(null, 20)).isEmpty();
    }

    @Test
    void findByUserId_shouldReturnMappedList() {
        when(emotionalEventJpaRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(entity()));

        List<EmotionalEvent> result = repository.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        verify(emotionalEventJpaRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void findRecommendationEventsByUserId_shouldReturnMappedList() {
        when(emotionalEventJpaRepository.findByUserIdAndRecommendationDecisionIsNotNullOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(entity()));

        List<EmotionalEvent> result = repository.findRecommendationEventsByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecommendationDecision()).isEqualTo(RecommendationDecision.ACCEPTED);
        verify(emotionalEventJpaRepository).findByUserIdAndRecommendationDecisionIsNotNullOrderByCreatedAtDesc(1L);
    }

    private EmotionalEvent domain() {
        Instant now = Instant.now();
        return EmotionalEvent.builder()
                .id(10L)
                .userId(1L)
                .source(EmotionalEventSource.CHATBOT)
                .inputText("texto")
                .detectedEmotion("ANSIEDAD")
                .confidence(0.9)
                .valence(-0.8)
                .arousal(0.9)
                .dominance(-0.7)
                .intensity(0.8)
                .userGoal("calmarme")
                .generatedRecommendation("Respira")
                .recommendedActivityId(2L)
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .chosenActivityId(2L)
                .feedbackScore(4)
                .feedbackText("mejor")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private EmotionalEventEntity entity() {
        Instant now = Instant.now();
        return EmotionalEventEntity.builder()
                .id(10L)
                .user(user(1L))
                .source(EmotionalEventSource.CHATBOT)
                .inputText("texto")
                .detectedEmotion("ANSIEDAD")
                .confidence(0.9)
                .valence(-0.8)
                .arousal(0.9)
                .dominance(-0.7)
                .intensity(0.8)
                .userGoal("calmarme")
                .generatedRecommendation("Respira")
                .recommendedActivity(activity(2L))
                .chosenActivity(activity(2L))
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .feedbackScore(4)
                .feedbackText("mejor")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private AppUserEntity user(Long id) {
        AppUserEntity user = new AppUserEntity();
        user.setId(id);
        return user;
    }

    private ActivityEntity activity(Long id) {
        ActivityEntity activity = new ActivityEntity();
        activity.setId(id);
        return activity;
    }
}
