package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class UserActivitiesResponse {
    private List<ActivitySessionDto> activitySessions;
    private long todayActivitiesCount;
    private String favoriteActivity;
    private String averageSessionsText;
    private Map<String, Integer> activityDistribution;
    private Map<String, String> activityNames;
}
