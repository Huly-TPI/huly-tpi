package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.extension.GetUserAntiScrollSettingsMapper;
import com.huly.backend.domain.mapper.extension.SaveExtensionMetricsMapper;
import com.huly.backend.domain.mapper.extension.SaveUserAntiScrollSettingsMapper;
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
    public GetUserAntiScrollSettingsMapper getUserAntiScrollSettingsMapper() {
        return new GetUserAntiScrollSettingsMapper();
    }

    @Bean
    public SaveUserAntiScrollSettingsMapper saveUserAntiScrollSettingsMapper() {
        return new SaveUserAntiScrollSettingsMapper();
    }

    @Bean
    public SaveExtensionMetricsMapper saveExtensionMetricsMapper() {
        return new SaveExtensionMetricsMapper();
    }

    @Bean
    public GetUserAntiScrollSettingsUseCase getUserAntiScrollSettingsUseCase(
            UserAntiScrollSettingsRepository settingsRepository,
            AntiScrollGlobalConfigRepository antiScrollConfigRepository,
            GetCurrentUserUseCase getCurrentUserUseCase,
            @Value("${frontend.url}") String frontendUrl,
            @Value("${backend.url}") String backendUrl,
            GetUserAntiScrollSettingsMapper getUserAntiScrollSettingsMapper
    ) {
        return new GetUserAntiScrollSettingsUseCase(
                settingsRepository,
                antiScrollConfigRepository,
                getCurrentUserUseCase,
                frontendUrl,
                backendUrl,
                getUserAntiScrollSettingsMapper
        );
    }

    @Bean
    public SaveUserAntiScrollSettingsUseCase saveUserAntiScrollSettingsUseCase(
            UserAntiScrollSettingsRepository settingsRepository,
            SaveUserAntiScrollSettingsMapper saveUserAntiScrollSettingsMapper
    ) {
        return new SaveUserAntiScrollSettingsUseCase(settingsRepository, saveUserAntiScrollSettingsMapper);
    }

    @Bean
    public SaveExtensionMetricsUseCase saveExtensionMetricsUseCase(
            ExtensionMetricsRepository metricsRepository,
            UserAntiScrollSettingsRepository settingsRepository,
            SaveExtensionMetricsMapper saveExtensionMetricsMapper
    ) {
        return new SaveExtensionMetricsUseCase(metricsRepository, settingsRepository, saveExtensionMetricsMapper);
    }
}
