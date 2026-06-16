package com.huly.backend.domain.useCase.admin.userActivities;

import java.util.List;
import java.util.Map;

public record GetUserActivitiesResponse(
        List<ActivitySessionResponse> activitySessions,
        long todayActivitiesCount,
        String favoriteActivity,
        String averageSessionsText,
        Map<String, Integer> activityDistribution
) {
}
