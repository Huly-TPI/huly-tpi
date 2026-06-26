package com.huly.backend.infrastructure.presentation.dto.emotionalState;

import java.time.Instant;

public record UserEmotionalStateResponse(
        Long id,
        Long userId,
        double valence,
        double arousal,
        double dominance,
        double intensity,
        String source,
        Instant timestamp
) {}
