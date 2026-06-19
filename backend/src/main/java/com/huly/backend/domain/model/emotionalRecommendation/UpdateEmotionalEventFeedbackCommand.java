package com.huly.backend.domain.model.emotionalRecommendation;

public record UpdateEmotionalEventFeedbackCommand(
        Integer feedbackScore,
        String feedbackText
) {}
