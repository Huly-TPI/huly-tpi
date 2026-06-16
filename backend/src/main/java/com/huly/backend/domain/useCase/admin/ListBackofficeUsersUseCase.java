package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.UserPlanRepository;
import com.huly.backend.domain.dto.payment.UserPlan;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.domain.model.EmotionalEvent;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ListBackofficeUsersUseCase {

    private final UserRepository userRepository;
    private final ExtensionSettingsRepository settingsRepository;
    private final ExtensionMetricsRepository metricsRepository;
    private final UserPlanRepository userPlanRepository;
    private final EmotionalEventRepository emotionalEventRepository;

    public List<BackofficeUserSummary> execute() {
        return execute(null);
    }

    public List<BackofficeUserSummary> execute(String search) {
        List<AppUser> users = userRepository.findAllNonAdmins();
        if (search != null && !search.isBlank()) {
            String lowercaseSearch = search.toLowerCase();
            users = users.stream()
                    .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(lowercaseSearch))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(lowercaseSearch)))
                    .toList();
        }

        List<BackofficeUserSummary> summaries = new ArrayList<>();

        for (AppUser user : users) {
            Optional<ExtensionSettings> settingsOpt = settingsRepository.findByUserId(user.getId());
            boolean antiScrollEnabled = settingsOpt.map(ExtensionSettings::isEnabled).orElse(false);
            boolean consent = settingsOpt.map(ExtensionSettings::isDataSharingConsent).orElse(false);

            int coins = userRepository.getCoins(user.getId());
            String plan = userPlanRepository.findByUser(user.getId())
                    .filter(p -> p.isActive(java.time.Instant.now()))
                    .map(UserPlan::getPlanCode)
                    .orElse("Gratuito");

            List<EmotionalEvent> emotionalEvents = emotionalEventRepository.findByUserId(user.getId());
            Map<String, Integer> emotionDistribution = new LinkedHashMap<>();
            for (EmotionalEvent event : emotionalEvents) {
                if (event.getDetectedEmotion() == null) 
                    continue;
                
                String emotion = event.getDetectedEmotion().trim().toUpperCase();
                emotionDistribution.put(emotion, emotionDistribution.getOrDefault(emotion, 0) + 1);
            }

            String dominantEmotion = "NEUTRAL";
            int maxEmotionCount = 0;
            for (Map.Entry<String, Integer> entry : emotionDistribution.entrySet()) {
                if (entry.getValue() > maxEmotionCount) {
                    maxEmotionCount = entry.getValue();
                    dominantEmotion = entry.getKey();
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
                    .mostUsedApp(null)
                    .mostUsedAppActiveSeconds(0)
                    .totalScrollTimeSeconds(0)
                    .dailyScrollTimeSeconds(Map.of())
                    .topApps(List.of())
                    .coins(coins)
                    .plan(plan)
                    .dominantEmotion(dominantEmotion)
                    .build());
        }

        return summaries;
    }
}
