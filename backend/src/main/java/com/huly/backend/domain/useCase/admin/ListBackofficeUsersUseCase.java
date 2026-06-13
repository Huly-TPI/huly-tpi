package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ListBackofficeUsersUseCase {

    private final UserRepository userRepository;
    private final ExtensionSettingsRepository settingsRepository;
    private final ExtensionMetricsRepository metricsRepository;

    public List<BackofficeUserSummary> execute() {
        List<AppUser> users = userRepository.findAllNonAdmins();
        List<BackofficeUserSummary> summaries = new ArrayList<>();

        for (AppUser user : users) {
            Optional<ExtensionSettings> settingsOpt = settingsRepository.findByUserId(user.getId());
            boolean antiScrollEnabled = settingsOpt.map(ExtensionSettings::isEnabled).orElse(false);
            boolean consent = settingsOpt.map(ExtensionSettings::isDataSharingConsent).orElse(false);

            String mostUsedApp = null;
            int mostUsedAppActiveSeconds = 0;
            int totalScrollTimeSeconds = 0;
            Map<String, Integer> dailyScrollTimeSeconds = new HashMap<>();
            List<TopAppStats> topApps = List.of();

            for (int i = 0; i < 7; i++) {
                dailyScrollTimeSeconds.put("current_" + i, 0);
                dailyScrollTimeSeconds.put("previous_" + i, 0);
            }

            if (consent) {
                List<ExtensionMetric> metrics = metricsRepository.findByUserId(user.getId());
                if (metrics != null && !metrics.isEmpty()) {
                    Map<String, Integer> domainTimes = metrics.stream()
                            .collect(Collectors.groupingBy(
                                    ExtensionMetric::getDomain,
                                    Collectors.summingInt(ExtensionMetric::getActiveSeconds)
                            ));

                    totalScrollTimeSeconds = domainTimes.values().stream().mapToInt(Integer::intValue).sum();

                    Optional<Map.Entry<String, Integer>> maxEntry = domainTimes.entrySet().stream()
                            .max(Map.Entry.comparingByValue());

                    if (maxEntry.isPresent()) {
                        mostUsedApp = maxEntry.get().getKey();
                        mostUsedAppActiveSeconds = maxEntry.get().getValue();
                    }

                    topApps = domainTimes.entrySet().stream()
                            .map(e -> new TopAppStats(e.getKey(), e.getValue()))
                            .sorted(Comparator.comparingInt(TopAppStats::getTotalActiveSeconds).reversed())
                            .limit(5)
                            .toList();

                    java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault());
                    java.time.ZonedDateTime startOfThisWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                            .truncatedTo(java.time.temporal.ChronoUnit.DAYS);
                    java.time.ZonedDateTime startOfPreviousWeek = startOfThisWeek.minusWeeks(1);

                    for (ExtensionMetric metric : metrics) {
                        if (metric.getCreatedAt() == null) continue;
                        java.time.ZonedDateTime createdAtZdt = metric.getCreatedAt().atZone(java.time.ZoneId.systemDefault());

                        if (!createdAtZdt.isBefore(startOfThisWeek) && createdAtZdt.isBefore(startOfThisWeek.plusWeeks(1))) {
                            int dayIndex = createdAtZdt.getDayOfWeek().getValue() - 1;
                            String key = "current_" + dayIndex;
                            dailyScrollTimeSeconds.put(key, dailyScrollTimeSeconds.getOrDefault(key, 0) + metric.getActiveSeconds());
                        } else if (!createdAtZdt.isBefore(startOfPreviousWeek) && createdAtZdt.isBefore(startOfThisWeek)) {
                            int dayIndex = createdAtZdt.getDayOfWeek().getValue() - 1;
                            String key = "previous_" + dayIndex;
                            dailyScrollTimeSeconds.put(key, dailyScrollTimeSeconds.getOrDefault(key, 0) + metric.getActiveSeconds());
                        }
                    }
                }
            }

            summaries.add(BackofficeUserSummary.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .birthDate(user.getBirthDate())
                    .antiScrollEnabled(antiScrollEnabled)
                    .dataSharingConsent(consent)
                    .mostUsedApp(mostUsedApp)
                    .mostUsedAppActiveSeconds(mostUsedAppActiveSeconds)
                    .totalScrollTimeSeconds(totalScrollTimeSeconds)
                    .dailyScrollTimeSeconds(dailyScrollTimeSeconds)
                    .topApps(topApps)
                    .build());
        }

        return summaries;
    }
}
