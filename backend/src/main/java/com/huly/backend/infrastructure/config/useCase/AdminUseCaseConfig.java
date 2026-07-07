package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.UserPersonalitySummaryRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.port.VectorMemoryPort;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.GetAntiScrollGlobalConfigUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollGlobalConfigUseCase;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsUseCase;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsUseCase;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;

import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.useCase.admin.activities.GetAdminActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.activities.UpdateActivityConfigUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivitiesKpiUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityPopularityUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityCorrelationUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityImpactUseCase;
import com.huly.backend.domain.mapper.activities.UpdateActivityConfigMapper;

@Configuration
public class AdminUseCaseConfig {

    @Bean
    public ListBackofficeUsersUseCase listBackofficeUsersUseCase(
            UserRepository userRepository,
            UserAntiScrollSettingsRepository settingsRepository,
            ExtensionMetricsRepository metricsRepository,
            UserPlanRepository userPlanRepository,
            EmotionalEventRepository emotionalEventRepository
    ) {
        return new ListBackofficeUsersUseCase(userRepository, settingsRepository, metricsRepository, userPlanRepository, emotionalEventRepository);
    }

    @Bean
    public GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase(
            UserRepository userRepository,
            UserAntiScrollSettingsRepository settingsRepository,
            ExtensionMetricsRepository metricsRepository
    ) {
        return new GetAntiScrollDashboardUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Bean
    public GetUserAntiScrollStatsUseCase getUserAntiScrollStatsUseCase(
            UserRepository userRepository,
            UserAntiScrollSettingsRepository settingsRepository,
            ExtensionMetricsRepository metricsRepository
    ) {
        return new GetUserAntiScrollStatsUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Bean
    public GetUserActivitiesUseCase getUserActivitiesUseCase(
            UserRepository userRepository,
            ActivitySessionRepository activitySessionRepository,
            ActivityRepository activityRepository
    ) {
        return new GetUserActivitiesUseCase(userRepository, activitySessionRepository, activityRepository);
    }

    @Bean
    public GetUserAiDiagnosticsUseCase getUserAiDiagnosticsUseCase(
            UserRepository userRepository,
            EmotionalEventRepository emotionalEventRepository,
            VectorMemoryPort vectorMemoryPort,
            UserPersonalitySummaryRepository userPersonalitySummaryRepository,
            ChatConversationPreferenceRepository chatConversationPreferenceRepository
    ) {
        return new GetUserAiDiagnosticsUseCase(
                userRepository,
                emotionalEventRepository,
                vectorMemoryPort,
                userPersonalitySummaryRepository,
                chatConversationPreferenceRepository
        );
    }

    @Bean
    public GetUserFinancialsUseCase getUserFinancialsUseCase(
            UserRepository userRepository,
            PaymentEventRepository paymentEventRepository,
            ProductRepository productRepository
    ) {
        return new GetUserFinancialsUseCase(userRepository, paymentEventRepository, productRepository);
    }

    @Bean
    public GetAntiScrollGlobalConfigUseCase getAntiScrollGlobalConfigUseCase(
            AntiScrollGlobalConfigRepository antiScrollConfigRepository
    ) {
        return new GetAntiScrollGlobalConfigUseCase(antiScrollConfigRepository);
    }

    @Bean
    public UpdateAntiScrollGlobalConfigUseCase updateAntiScrollGlobalConfigUseCase(
            AntiScrollGlobalConfigRepository antiScrollConfigRepository
    ) {
        return new UpdateAntiScrollGlobalConfigUseCase(antiScrollConfigRepository);
    }

    @Bean
    public GetAdminActivitiesUseCase getAdminActivitiesUseCase(ActivityRepository activityRepository) {
        return new GetAdminActivitiesUseCase(activityRepository);
    }

    @Bean
    public UpdateActivityConfigMapper updateActivityConfigMapper() {
        return new UpdateActivityConfigMapper();
    }

    @Bean
    public UpdateActivityConfigUseCase updateActivityConfigUseCase(
            ActivityRepository activityRepository,
            UpdateActivityConfigMapper updateActivityConfigMapper
    ) {
        return new UpdateActivityConfigUseCase(activityRepository, updateActivityConfigMapper);
    }

    @Bean
    public GetActivitiesKpiUseCase getActivitiesKpiUseCase(
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository,
            ActivitySessionRepository activitySessionRepository
    ) {
        return new GetActivitiesKpiUseCase(activityRepository, emotionalEventRepository, activitySessionRepository);
    }

    @Bean
    public GetActivityPopularityUseCase getActivityPopularityUseCase(
            ActivityRepository activityRepository,
            ActivitySessionRepository activitySessionRepository
    ) {
        return new GetActivityPopularityUseCase(activityRepository, activitySessionRepository);
    }

    @Bean
    public GetActivityCorrelationUseCase getActivityCorrelationUseCase(
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository
    ) {
        return new GetActivityCorrelationUseCase(activityRepository, emotionalEventRepository);
    }

    @Bean
    public GetActivityImpactUseCase getActivityImpactUseCase(
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository
    ) {
        return new GetActivityImpactUseCase(activityRepository, emotionalEventRepository);
    }
}
