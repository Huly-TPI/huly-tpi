package com.huly.backend.domain.useCase.admin.dashboard;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RequiredArgsConstructor
public class GetAdminDashboardUseCase {

    private final UserRepository userRepository;
    private final UserAntiScrollSettingsRepository settingsRepository;
    private final UserDetailDomainRepository userDetailRepository;
    private final ActivitySessionRepository activitySessionRepository;

    public GetAdminDashboardResponse execute() {
        List<AppUser> users = userRepository.findAllNonAdmins();
        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        int activeUsers = countActiveExtensionUsers(users);
        int usersRegisteredThisWeek = countUsersRegisteredSince(users, oneWeekAgo);
        int activitiesThisWeek = countActivitiesCompletedSince(oneWeekAgo);

        return GetAdminDashboardResponse.builder()
                .activeExtensionUsersCount(activeUsers)
                .usersRegisteredThisWeek(usersRegisteredThisWeek)
                .activitiesThisWeek(activitiesThisWeek)
                .build();
    }

    private int countActiveExtensionUsers(List<AppUser> users) {
        int activeUsers = 0;
        for (AppUser user : users) {
            Optional<UserAntiScrollSettings> settingsOpt = settingsRepository.findByUserId(user.getId());
            if (settingsOpt.isPresent() && settingsOpt.get().isEnabled()) {
                activeUsers++;
            }
        }
        return activeUsers;
    }

    private int countUsersRegisteredSince(List<AppUser> users, Instant start) {
        long count = users.stream()
                .map(user -> userDetailRepository.findUserCreatedAt(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(createdAt -> createdAt.isAfter(start))
                .count();
        return (int) count;
    }

    private int countActivitiesCompletedSince(Instant start) {
        return activitySessionRepository.findAllAfter(start).size();
    }
}
