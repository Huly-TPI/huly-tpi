package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class GetExtensionSettingsUseCase {
    private final ExtensionSettingsRepository settingsRepository;
    
    @Value("${frontend.url}")
    private String frontendUrl;
    
    @Value("${backend.url}")
    private String backendUrl;

    public ExtensionSettings execute(Long userId) {
        return settingsRepository.findByUserId(userId)
                .orElse(ExtensionSettings.builder()
                        .enabled(true)
                        .pauseIntervalMinutes(20)
                        .gardenUrl(frontendUrl + "/garden")
                        .backendUrl(backendUrl)
                        .monitoredDomains(java.util.List.of("twitter.com", "x.com", "instagram.com", "tiktok.com", "youtube.com", "facebook.com"))
                        .build());
    }
}
