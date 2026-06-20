package com.huly.backend.domain.repository.chatBotConfig;

import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;

import java.util.List;
import java.util.Optional;

public interface EmotionalEventRepository {
    EmotionalEvent save(EmotionalEvent event);

    Optional<EmotionalEvent> findById(Long id);

    List<EmotionalEvent> findRecentRecommendationHistoryByUserId(Long userId, int limit);

    List<EmotionalEvent> findByUserId(Long userId);

    List<EmotionalEvent> findRecommendationEventsByUserId(Long userId);
}
