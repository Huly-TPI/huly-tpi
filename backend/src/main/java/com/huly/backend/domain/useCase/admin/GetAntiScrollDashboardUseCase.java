package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.admin.AntiScrollDashboardStats;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class GetAntiScrollDashboardUseCase {

    private final UserRepository userRepository;
    private final UserAntiScrollSettingsRepository settingsRepository;
    private final ExtensionMetricsRepository metricsRepository;

    public AntiScrollDashboardStats execute() {
        List<AppUser> users = userRepository.findAllNonAdmins();
        int totalUsers = users.size();
        int activeUsers = 0;
        int consentUsers = 0;

        for (AppUser user : users) {
            Optional<UserAntiScrollSettings> settingsOpt = settingsRepository.findByUserId(user.getId());
            if (settingsOpt.isPresent()) {
                if (settingsOpt.get().isEnabled()) {
                    activeUsers++;
                }
                if (settingsOpt.get().isDataSharingConsent()) {
                    consentUsers++;
                }
            }
        }

        List<ExtensionMetric> metrics = metricsRepository.findAllConsentingMetrics();
        int totalModals = 0;
        int totalRedirects = 0;

        Map<String, Integer> domainTimes = new HashMap<>();

        for (ExtensionMetric m : metrics) {
            totalModals += m.getModalsShown();
            totalRedirects += m.getRedirects();
            domainTimes.put(m.getDomain(), domainTimes.getOrDefault(m.getDomain(), 0) + m.getActiveSeconds());
        }

        List<TopAppStats> topApps = domainTimes.entrySet().stream()
                .map(e -> new TopAppStats(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(TopAppStats::getTotalActiveSeconds).reversed())
                .limit(10)
                .toList();

        return AntiScrollDashboardStats.builder()
                .totalModalsShown(totalModals)
                .totalRedirects(totalRedirects)
                .totalUsersCount(totalUsers)
                .activeExtensionUsersCount(activeUsers)
                .dataSharingConsentUsersCount(consentUsers)
                .topUsedApps(topApps)
                .build();
    }
}
