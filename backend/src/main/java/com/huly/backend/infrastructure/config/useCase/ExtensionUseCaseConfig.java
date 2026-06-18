package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.domain.useCase.extension.GetUserAntiScrollSettingsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionMetricsUseCase;
import com.huly.backend.domain.useCase.extension.SaveUserAntiScrollSettingsUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtensionUseCaseConfig {

    @Bean
    public GetUserAntiScrollSettingsUseCase getUserAntiScrollSettingsUseCase(
            UserAntiScrollSettingsRepository settingsRepository,
            AntiScrollGlobalConfigRepository antiScrollConfigRepository,
            GetCurrentUserUseCase getCurrentUserUseCase,
            @Value("${frontend.url}") String frontendUrl,
            @Value("${backend.url}") String backendUrl
    ) {
        return new GetUserAntiScrollSettingsUseCase(
                settingsRepository,
                antiScrollConfigRepository,
                getCurrentUserUseCase,
                frontendUrl,
                backendUrl
        );
    }

    @Bean
    public SaveUserAntiScrollSettingsUseCase saveUserAntiScrollSettingsUseCase(UserAntiScrollSettingsRepository settingsRepository) {
        return new SaveUserAntiScrollSettingsUseCase(settingsRepository);
    }

    @Bean
    public SaveExtensionMetricsUseCase saveExtensionMetricsUseCase(
            ExtensionMetricsRepository metricsRepository,
            UserAntiScrollSettingsRepository settingsRepository
    ) {
        return new SaveExtensionMetricsUseCase(metricsRepository, settingsRepository);
    }
}
