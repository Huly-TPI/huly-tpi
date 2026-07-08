package com.huly.backend.domain.model.activity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityPopularityStats {
    private final String activityType;
    private final String activityName;
    private final long totalSessions;
}
