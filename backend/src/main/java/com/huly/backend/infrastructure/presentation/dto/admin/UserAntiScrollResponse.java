package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class UserAntiScrollResponse {
    private boolean antiScrollEnabled;
    private boolean dataSharingConsent;
    private String mostUsedApp;
    private Integer mostUsedAppActiveSeconds;
    private Integer totalScrollTimeSeconds;
    private Map<String, Integer> dailyScrollTimeSeconds;
    private List<TopAppResponse> topApps;
}
