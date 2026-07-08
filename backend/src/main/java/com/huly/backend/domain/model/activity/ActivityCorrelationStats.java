package com.huly.backend.domain.model.activity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityCorrelationStats {
    private final String activityType;
    private final String emotion;
    private final long suggestionsCount;
    private final double acceptanceRate;
}
