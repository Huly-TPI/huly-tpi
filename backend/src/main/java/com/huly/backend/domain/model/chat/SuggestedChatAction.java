package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.ActivityType;

public record SuggestedChatAction(
        ActivityType type,
        Long activityId,
        String title,
        String description,
        String actionUrl,
        Long emotionalEventId
) {
}
