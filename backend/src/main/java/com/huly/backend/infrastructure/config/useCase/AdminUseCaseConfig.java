package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminUseCaseConfig {

    @Bean
    public ListBackofficeUsersUseCase listBackofficeUsersUseCase(
            UserRepository userRepository,
            ExtensionSettingsRepository settingsRepository,
            ExtensionMetricsRepository metricsRepository
    ) {
        return new ListBackofficeUsersUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Bean
    public GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase(
            UserRepository userRepository,
            ExtensionSettingsRepository settingsRepository,
            ExtensionMetricsRepository metricsRepository
    ) {
        return new GetAntiScrollDashboardUseCase(userRepository, settingsRepository, metricsRepository);
    }
}
