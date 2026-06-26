package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.dto.extension.SaveExtensionMetricsRequest;
import com.huly.backend.domain.dto.extension.SaveExtensionMetricsResponse;
import com.huly.backend.domain.mapper.extension.SaveExtensionMetricsMapper;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class SaveExtensionMetricsUseCase {
    private final ExtensionMetricsRepository metricsRepository;
    private final UserAntiScrollSettingsRepository settingsRepository;
    private final SaveExtensionMetricsMapper mapper;

    public SaveExtensionMetricsResponse execute(SaveExtensionMetricsRequest request) {
        Long userId = request.userId();
        boolean consented = settingsRepository.findByUserId(userId)
                .map(UserAntiScrollSettings::isDataSharingConsent)
                .orElse(false);
        if (consented) {
            List<ExtensionMetric> metrics = mapper.toModel(request);
            metricsRepository.saveAll(userId, metrics);
        }
        return mapper.toResponse();
    }
}
