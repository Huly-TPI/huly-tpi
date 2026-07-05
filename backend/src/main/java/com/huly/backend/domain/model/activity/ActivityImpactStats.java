package com.huly.backend.domain.model.activity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityImpactStats {
    private final String activityType;
    private final double averageValenceChange;
    private final double averageArousalChange;
    private final boolean basedOnMetrics;
}
