package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionalEventEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IEmotionalEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class EmotionalEventRepositoryImpl implements EmotionalEventRepository {

    private final IEmotionalEventJpaRepository emotionalEventJpaRepository;
    private final AppUserRepository appUserRepository;
    private final IActivityJpaRepository activityJpaRepository;

    @Override
    public EmotionalEvent save(EmotionalEvent event) {
        EmotionalEventEntity savedEntity = emotionalEventJpaRepository.save(toEntity(event));
        return toDomain(savedEntity);
    }

    @Override
    public Optional<EmotionalEvent> findById(Long id) {
        return emotionalEventJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<EmotionalEvent> findRecentRecommendationHistoryByUserId(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }
        return emotionalEventJpaRepository.findRecommendationHistoryByUserId(userId, PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<EmotionalEvent> findByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return emotionalEventJpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<EmotionalEvent> findRecommendationEventsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return emotionalEventJpaRepository.findByUserIdAndRecommendationDecisionIsNotNullOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    private EmotionalEventEntity toEntity(EmotionalEvent event) {
        return EmotionalEventEntity.builder()
                .id(event.getId())
                .user(userReference(event.getUserId()))
                .source(event.getSource())
                .inputText(event.getInputText())
                .detectedEmotion(event.getDetectedEmotion())
                .confidence(event.getConfidence())
                .valence(event.getValence())
                .arousal(event.getArousal())
                .dominance(event.getDominance())
                .intensity(event.getIntensity())
                .userGoal(event.getUserGoal())
                .generatedRecommendation(event.getGeneratedRecommendation())
                .recommendedActivity(activityReference(event.getRecommendedActivityId()))
                .chosenActivity(activityReference(event.getChosenActivityId()))
                .recommendationDecision(event.getRecommendationDecision())
                .feedbackScore(event.getFeedbackScore())
                .feedbackText(event.getFeedbackText())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private EmotionalEvent toDomain(EmotionalEventEntity entity) {
        return EmotionalEvent.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .source(entity.getSource())
                .inputText(entity.getInputText())
                .detectedEmotion(entity.getDetectedEmotion())
                .confidence(entity.getConfidence())
                .valence(entity.getValence())
                .arousal(entity.getArousal())
                .dominance(entity.getDominance())
                .intensity(entity.getIntensity())
                .userGoal(entity.getUserGoal())
                .generatedRecommendation(entity.getGeneratedRecommendation())
                .recommendedActivityId(entity.getRecommendedActivity() != null ? entity.getRecommendedActivity().getId() : null)
                .chosenActivityId(entity.getChosenActivity() != null ? entity.getChosenActivity().getId() : null)
                .recommendationDecision(entity.getRecommendationDecision())
                .feedbackScore(entity.getFeedbackScore())
                .feedbackText(entity.getFeedbackText())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private AppUserEntity userReference(Long userId) {
        return userId != null ? appUserRepository.getReferenceById(userId) : null;
    }

    private ActivityEntity activityReference(Long activityId) {
        return activityId != null ? activityJpaRepository.getReferenceById(activityId) : null;
    }
}
