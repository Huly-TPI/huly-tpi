package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class BackofficeUserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private LocalDate birthDate;
    private boolean antiScrollEnabled;
    private boolean dataSharingConsent;
    private String mostUsedApp;
    private Integer mostUsedAppActiveSeconds;
    private Integer totalScrollTimeSeconds;
    private Map<String, Integer> dailyScrollTimeSeconds;
    private List<TopAppResponse> topApps;
    private Integer coins;
    private String plan;
}
