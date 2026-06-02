package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.EmotionalEvent;

import java.util.Optional;

public interface EmotionalEventRepository {
    EmotionalEvent save(EmotionalEvent event);

    Optional<EmotionalEvent> findById(Long id);
}
