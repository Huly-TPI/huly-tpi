package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class SaveExtensionMetricsUseCase {
    private final ExtensionMetricsRepository metricsRepository;
    private final ExtensionSettingsRepository settingsRepository;

    public void execute(Long userId, List<ExtensionMetric> metrics) {
        boolean consented = settingsRepository.findByUserId(userId)
                .map(ExtensionSettings::isDataSharingConsent)
                .orElse(false);
        if (consented) {
            metricsRepository.saveAll(userId, metrics);
        }
    }
}
