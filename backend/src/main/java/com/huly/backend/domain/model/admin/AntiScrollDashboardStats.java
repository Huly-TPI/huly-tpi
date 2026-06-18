package com.huly.backend.domain.model.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AntiScrollDashboardStats {
    private int totalModalsShown;
    private int totalRedirects;
    private int totalUsersCount;
    private int activeExtensionUsersCount;
    private int dataSharingConsentUsersCount;
    private List<TopAppStats> topUsedApps;
}
