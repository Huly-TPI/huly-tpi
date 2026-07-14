package com.huly.backend.domain.useCase.admin.antiscroll;

import com.huly.backend.domain.model.user.AppUser;
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

    public GetAntiScrollDashboardResponse execute() {
        List<AppUser> users = userRepository.findAllNonAdmins();
        List<ExtensionMetric> metrics = metricsRepository.findAllConsentingMetrics();

        AntiScrollUserCounts userCounts = countAntiScrollUserSettings(users);
        ExtensionMetricTotals metricTotals = calculateMetricTotals(metrics);
        List<TopAppStats> topApps = getTopUsedApps(metrics);

        return GetAntiScrollDashboardResponse.builder()
                .totalModalsShown(metricTotals.modalsShown())
                .totalRedirects(metricTotals.redirects())
                .totalUsersCount(users.size())
                .activeExtensionUsersCount(userCounts.activeUsers())
                .dataSharingConsentUsersCount(userCounts.consentUsers())
                .topUsedApps(topApps)
                .build();
    }

    private AntiScrollUserCounts countAntiScrollUserSettings(List<AppUser> users) {
        int activeUsers = 0;
        int consentUsers = 0;

        for (AppUser user : users) {
            Optional<UserAntiScrollSettings> settingsOpt = settingsRepository.findByUserId(user.getId());
            if (settingsOpt.isPresent()) {
                UserAntiScrollSettings settings = settingsOpt.get();
                if (settings.isEnabled()) {
                    activeUsers++;
                }
                if (settings.isDataSharingConsent()) {
                    consentUsers++;
                }
            }
        }
        return new AntiScrollUserCounts(activeUsers, consentUsers);
    }

    private ExtensionMetricTotals calculateMetricTotals(List<ExtensionMetric> metrics) {
        int totalModals = 0;
        int totalRedirects = 0;

        for (ExtensionMetric m : metrics) {
            totalModals += m.getModalsShown();
            totalRedirects += m.getRedirects();
        }
        return new ExtensionMetricTotals(totalModals, totalRedirects);
    }

    private List<TopAppStats> getTopUsedApps(List<ExtensionMetric> metrics) {
        Map<String, Integer> domainTimes = new HashMap<>();

        for (ExtensionMetric m : metrics) {
            domainTimes.put(m.getDomain(), domainTimes.getOrDefault(m.getDomain(), 0) + m.getActiveSeconds());
        }

        return domainTimes.entrySet().stream()
                .map(e -> new TopAppStats(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(TopAppStats::getTotalActiveSeconds).reversed())
                .limit(10)
                .toList();
    }

    private record AntiScrollUserCounts(int activeUsers, int consentUsers) {}
    private record ExtensionMetricTotals(int modalsShown, int redirects) {}
}
