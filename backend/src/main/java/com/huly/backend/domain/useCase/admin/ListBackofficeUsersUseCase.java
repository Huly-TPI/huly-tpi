package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
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
                    .build());
        }

        return summaries;
    }
}
