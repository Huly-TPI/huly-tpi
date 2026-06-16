package com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation;

import com.huly.backend.domain.model.enums.EmotionalEventSource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmotionalRecommendationRequest(
        Long userId,
        EmotionalEventSource source,
        @Size(max = 4000)
        String inputText,

        @NotBlank
        String detectedEmotion,

        @DecimalMin("0.0") @DecimalMax("1.0")
        Double confidence,

        @NotNull
        @DecimalMin("-1.0") @DecimalMax("1.0")
        Double valence,

        @NotNull
        @DecimalMin("-1.0") @DecimalMax("1.0")
        Double arousal,

        @NotNull
        @DecimalMin("-1.0") @DecimalMax("1.0")
        Double dominance,

        @NotNull
        @DecimalMin("0.0") @DecimalMax("1.0")
        Double intensity,

        @Size(max = 255)
        String userGoal
) {}
