package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.model.extension.AntiScrollConfig;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.repository.extension.AntiScrollConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@RequiredArgsConstructor
public class GetExtensionSettingsUseCase {
    private final ExtensionSettingsRepository settingsRepository;
    private final AntiScrollConfigRepository antiScrollConfigRepository;
    
    @Value("${frontend.url}")
    private String frontendUrl;
    
    @Value("${backend.url}")
    private String backendUrl;

    public ExtensionSettings execute(Long userId) {
        int defaultInterval = antiScrollConfigRepository.findFirst()
                .map(AntiScrollConfig::getDefaultPauseIntervalMinutes)
                .orElse(20);

        return settingsRepository.findByUserId(userId)
                .orElse(ExtensionSettings.builder()
                        .enabled(true)
                        .pauseIntervalSeconds(defaultInterval * 60)
                        .gardenUrl(frontendUrl + "/")
                        .backendUrl(backendUrl)
                        .monitoredDomains(List.of("twitter.com", "x.com", "instagram.com", "tiktok.com", "youtube.com", "facebook.com"))
                        .build());
    }
}
