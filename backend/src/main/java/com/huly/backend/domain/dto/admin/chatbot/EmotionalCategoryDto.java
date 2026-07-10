package com.huly.backend.domain.dto.admin.chatbot;

public record EmotionalCategoryDto(
        String name,
        int detections,
        int detect,
        String severity
) {}
