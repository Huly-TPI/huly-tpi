package com.huly.backend.domain.useCase.admin.userAntiScroll;

import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetUserAntiScrollStatsUseCase {
    private static final Set<String> SECOND_LEVEL_TLDS = Set.of("ac", "co", "com", "edu", "gov", "net", "org");

    private final UserRepository userRepository;
    private final UserAntiScrollSettingsRepository settingsRepository;
    private final ExtensionMetricsRepository metricsRepository;

    public GetUserAntiScrollStatsResponse execute(GetUserAntiScrollStatsRequest request) {
        Long userId = request.userId();

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<UserAntiScrollSettings> settingsOpt = settingsRepository.findByUserId(userId);
        boolean antiScrollEnabled = settingsOpt.map(UserAntiScrollSettings::isEnabled).orElse(false);
        boolean consent = settingsOpt.map(UserAntiScrollSettings::isDataSharingConsent).orElse(false);
        List<String> monitoredDomains = settingsOpt
                .map(UserAntiScrollSettings::getMonitoredDomains)
                .orElse(List.of());

        String mostUsedApp = null;
        int mostUsedAppActiveSeconds = 0;
        int totalScrollTimeSeconds = 0;
        Map<String, Integer> dailyScrollTimeSeconds = new HashMap<>();
        List<TopAppStats> topApps = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            dailyScrollTimeSeconds.put("current_" + i, 0);
            dailyScrollTimeSeconds.put("previous_" + i, 0);
        }

        if (consent) {
            List<ExtensionMetric> metrics = metricsRepository.findByUserId(userId);
            if (metrics != null && !metrics.isEmpty()) {
                java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault());
                java.time.ZonedDateTime startOfThisWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                        .truncatedTo(java.time.temporal.ChronoUnit.DAYS);
                java.time.ZonedDateTime startOfPreviousWeek = startOfThisWeek.minusWeeks(1);
                List<ExtensionMetric> filteredMetrics = metrics.stream()
                        .filter(metric -> matchesMonitoredDomains(metric.getDomain(), monitoredDomains))
                        .map(metric -> ExtensionMetric.builder()
                                .domain(normalizeDomain(metric.getDomain()))
                                .activeSeconds(metric.getActiveSeconds())
                                .scrollCount(metric.getScrollCount())
                                .modalsShown(metric.getModalsShown())
                                .redirects(metric.getRedirects())
                                .createdAt(metric.getCreatedAt())
                                .build())
                        .toList();

                for (ExtensionMetric metric : filteredMetrics) {
                    if (metric.getCreatedAt() == null) {
                        continue;
                    }

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

                List<ExtensionMetric> requestedMetrics = filteredMetrics.stream()
                        .filter(metric -> belongsToRequestedPeriod(metric, request, startOfThisWeek, startOfPreviousWeek))
                        .toList();

                Map<String, Integer> domainTimes = requestedMetrics.stream()
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
            }
        }

        return new GetUserAntiScrollStatsResponse(
                antiScrollEnabled,
                consent,
                mostUsedApp,
                mostUsedAppActiveSeconds,
                totalScrollTimeSeconds,
                dailyScrollTimeSeconds,
                topApps
        );
    }

    private boolean belongsToRequestedPeriod(
            ExtensionMetric metric,
            GetUserAntiScrollStatsRequest request,
            java.time.ZonedDateTime startOfThisWeek,
            java.time.ZonedDateTime startOfPreviousWeek
    ) {
        if (metric.getCreatedAt() == null) {
            return false;
        }

        java.time.ZonedDateTime createdAt = metric.getCreatedAt().atZone(java.time.ZoneId.systemDefault());
        boolean currentWeek = !"previous".equalsIgnoreCase(request.week());
        java.time.ZonedDateTime start = currentWeek ? startOfThisWeek : startOfPreviousWeek;
        java.time.ZonedDateTime end = currentWeek ? startOfThisWeek.plusWeeks(1) : startOfThisWeek;

        if (createdAt.isBefore(start) || !createdAt.isBefore(end)) {
            return false;
        }

        if (request.day() == null || "all".equalsIgnoreCase(request.day())) {
            return true;
        }

        try {
            int requestedDayIndex = Integer.parseInt(request.day());
            return createdAt.getDayOfWeek().getValue() - 1 == requestedDayIndex;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    private boolean matchesMonitoredDomains(String metricDomain, List<String> monitoredDomains) {
        if (metricDomain == null || monitoredDomains == null || monitoredDomains.isEmpty()) {
            return false;
        }

        String currentClean = stripCountrySuffix(normalizeDomain(metricDomain));
        return monitoredDomains.stream()
                .map(this::normalizeDomain)
                .map(this::stripCountrySuffix)
                .anyMatch(monitoredClean -> currentClean.equals(monitoredClean));
    }

    private String stripCountrySuffix(String domain) {
        return domain.replaceAll("\\.(com|org|net|edu|gov|co)\\.[a-zA-Z]{2}$", ".$1");
    }

    private String normalizeDomain(String value) {
        String domain = value == null ? "" : value.trim().toLowerCase().replaceAll(":\\d+$", "").replaceAll("\\.$", "");
        if (domain.isEmpty() || domain.equals("localhost") || domain.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
            return domain;
        }

        String[] labels = Arrays.stream(domain.split("\\."))
                .filter(label -> !label.isBlank())
                .toArray(String[]::new);

        if (labels.length <= 2) {
            return domain.replaceFirst("^www\\.", "");
        }

        String last = labels[labels.length - 1];
        String secondLast = labels[labels.length - 2];
        String thirdLast = labels[labels.length - 3];

        if (last.length() == 2 && SECOND_LEVEL_TLDS.contains(secondLast)) {
            return thirdLast + "." + secondLast + "." + last;
        }

        return secondLast + "." + last;
    }
}
