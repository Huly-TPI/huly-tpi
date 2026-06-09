package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.ActivityType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ActivitySession {
    private Long id;
    private Long userId;
    private ActivityType activityType;
    private Instant createdAt;
}
