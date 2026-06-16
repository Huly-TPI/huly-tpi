package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.ActivitySessionRepository;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.domain.repository.PaymentEventRepository;
import com.huly.backend.domain.repository.ProductRepository;
import com.huly.backend.domain.repository.VectorMemoryRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsUseCase;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsUseCase;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;

import com.huly.backend.domain.repository.UserPlanRepository;

@Configuration
public class AdminUseCaseConfig {

    @Bean
    public ListBackofficeUsersUseCase listBackofficeUsersUseCase(
            UserRepository userRepository,
            ExtensionSettingsRepository settingsRepository,
            ExtensionMetricsRepository metricsRepository,
            UserPlanRepository userPlanRepository,
            EmotionalEventRepository emotionalEventRepository
    ) {
        return new ListBackofficeUsersUseCase(userRepository, settingsRepository, metricsRepository, userPlanRepository, emotionalEventRepository);
    }

    @Bean
    public GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase(
            UserRepository userRepository,
            ExtensionSettingsRepository settingsRepository,
            ExtensionMetricsRepository metricsRepository
    ) {
        return new GetAntiScrollDashboardUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Bean
    public GetUserAntiScrollStatsUseCase getUserAntiScrollStatsUseCase(
            UserRepository userRepository,
            ExtensionSettingsRepository settingsRepository,
            ExtensionMetricsRepository metricsRepository
    ) {
        return new GetUserAntiScrollStatsUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Bean
    public GetUserActivitiesUseCase getUserActivitiesUseCase(
            UserRepository userRepository,
            ActivitySessionRepository activitySessionRepository
    ) {
        return new GetUserActivitiesUseCase(userRepository, activitySessionRepository);
    }

    @Bean
    public GetUserAiDiagnosticsUseCase getUserAiDiagnosticsUseCase(
            UserRepository userRepository,
            EmotionalEventRepository emotionalEventRepository,
            VectorMemoryRepository vectorMemoryRepository,
            ChatConversationPreferenceRepository chatConversationPreferenceRepository
    ) {
        return new GetUserAiDiagnosticsUseCase(userRepository, emotionalEventRepository, vectorMemoryRepository, chatConversationPreferenceRepository);
    }

    @Bean
    public GetUserFinancialsUseCase getUserFinancialsUseCase(
            UserRepository userRepository,
            PaymentEventRepository paymentEventRepository,
            ProductRepository productRepository
    ) {
        return new GetUserFinancialsUseCase(userRepository, paymentEventRepository, productRepository);
    }
}
