package com.huly.backend.presentation.dto.emotionalEvent;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record EmotionalEventFeedbackRequest(
        @Min(1) @Max(5)
        Integer feedbackScore,

        @Size(max = 2000)
        String feedbackText
) {}
