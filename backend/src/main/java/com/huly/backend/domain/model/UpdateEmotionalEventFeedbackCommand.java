package com.huly.backend.domain.model;

public record UpdateEmotionalEventFeedbackCommand(
        Integer feedbackScore,
        String feedbackText
) {}
