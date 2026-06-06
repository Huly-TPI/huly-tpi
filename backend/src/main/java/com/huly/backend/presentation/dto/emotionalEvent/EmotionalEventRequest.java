package com.huly.backend.presentation.dto.emotionalEvent;

import com.huly.backend.domain.model.enums.EmotionalEventSource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmotionalEventRequest(
        Long userId,

        @NotNull
        EmotionalEventSource source,

        @Size(max = 4000)
        String inputText,

        @NotBlank
        String detectedEmotion,

        @DecimalMin("0.0") @DecimalMax("1.0")
        Double confidence,

        @DecimalMin("-1.0") @DecimalMax("1.0")
        Double valence,

        @DecimalMin("-1.0") @DecimalMax("1.0")
        Double arousal,

        @DecimalMin("-1.0") @DecimalMax("1.0")
        Double dominance,

        @DecimalMin("0.0") @DecimalMax("1.0")
        Double intensity,

        @Size(max = 255)
        String userGoal,

        @Size(max = 4000)
        String generatedRecommendation,

        Long recommendedActivityId,
        Long chosenActivityId
) {}
