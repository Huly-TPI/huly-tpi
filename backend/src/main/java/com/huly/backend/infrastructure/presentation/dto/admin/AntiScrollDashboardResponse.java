package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AntiScrollDashboardResponse {
    private int totalModalsShown;
    private int totalRedirects;
    private int totalUsersCount;
    private int activeExtensionUsersCount;
    private int dataSharingConsentUsersCount;
    private List<TopAppResponse> topUsedApps;
}
