package com.huly.backend.domain.useCase.admin.userAntiScroll;

import com.huly.backend.domain.model.admin.TopAppStats;
import java.util.List;
import java.util.Map;

public record GetUserAntiScrollStatsResponse(
        boolean antiScrollEnabled,
        boolean dataSharingConsent,
        String mostUsedApp,
        Integer mostUsedAppActiveSeconds,
        Integer totalScrollTimeSeconds,
        Map<String, Integer> dailyScrollTimeSeconds,
        List<TopAppStats> topApps
) {
}
