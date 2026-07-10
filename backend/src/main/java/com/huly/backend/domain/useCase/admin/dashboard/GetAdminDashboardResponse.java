package com.huly.backend.domain.useCase.admin.dashboard;

import lombok.Builder;

@Builder
public record GetAdminDashboardResponse(
    int activeExtensionUsersCount,
    int usersRegisteredThisWeek,
    int activitiesThisWeek
) {}
