package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.EmotionalEvent;

import java.util.List;
import java.util.Optional;

public interface EmotionalEventRepository {
    EmotionalEvent save(EmotionalEvent event);

    Optional<EmotionalEvent> findById(Long id);

    List<EmotionalEvent> findRecentRecommendationHistoryByUserId(Long userId, int limit);
}
