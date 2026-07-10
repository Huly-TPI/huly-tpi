package com.huly.backend.domain.useCase.admin.antiscroll;

import com.huly.backend.domain.model.admin.TopAppStats;
import lombok.Builder;
import java.util.List;

@Builder
public record GetAntiScrollDashboardResponse(
    int totalModalsShown,
    int totalRedirects,
    int totalUsersCount,
    int activeExtensionUsersCount,
    int dataSharingConsentUsersCount,
    List<TopAppStats> topUsedApps
) {}
