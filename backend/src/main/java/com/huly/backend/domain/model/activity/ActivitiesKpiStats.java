package com.huly.backend.domain.model.activity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivitiesKpiStats {
    private final long totalSessions;
    private final String topActivityType;
    private final long topActivitySessions;
    private final double averageMoodImprovement;
}
