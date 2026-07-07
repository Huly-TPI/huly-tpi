package com.huly.backend.domain.model.activity;

import com.huly.backend.domain.model.enums.ActivityType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ActivityMetric {
    private final ActivityType activityType;
    private final String activityName;
    private final long totalRecommendations;
    private final double acceptanceRate;
    private final double rejectionRate;
    private final double averageSatisfaction;
    private final long totalSessions;
    private final double moodImprovementRate;
    private final double averageValenceChange;
    private final double averageArousalChange;
    private final Map<String, Long> decisionsDistribution;
    private final Map<String, Long> emotionDistribution;
}
