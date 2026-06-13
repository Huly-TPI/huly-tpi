package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.useCase.extension.GetExtensionSettingsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionMetricsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionSettingsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtensionUseCaseConfig {

    @Bean
    public GetExtensionSettingsUseCase getExtensionSettingsUseCase(
            ExtensionSettingsRepository settingsRepository,
            com.huly.backend.domain.repository.extension.AntiScrollConfigRepository antiScrollConfigRepository
    ) {
        return new GetExtensionSettingsUseCase(settingsRepository, antiScrollConfigRepository);
    }

    @Bean
    public SaveExtensionSettingsUseCase saveExtensionSettingsUseCase(ExtensionSettingsRepository settingsRepository) {
        return new SaveExtensionSettingsUseCase(settingsRepository);
    }

    @Bean
    public SaveExtensionMetricsUseCase saveExtensionMetricsUseCase(
            ExtensionMetricsRepository metricsRepository,
            ExtensionSettingsRepository settingsRepository
    ) {
        return new SaveExtensionMetricsUseCase(metricsRepository, settingsRepository);
    }
}
