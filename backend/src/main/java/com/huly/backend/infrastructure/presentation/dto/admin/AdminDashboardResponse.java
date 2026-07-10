package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {
    private int activeExtensionUsersCount;
    private int usersRegisteredThisWeek;
    private int activitiesThisWeek;
}
