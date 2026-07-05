package com.huly.backend.infrastructure.presentation.mapper;

import com.huly.backend.domain.model.admin.AntiScrollDashboardStats;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.GetAntiScrollGlobalConfigResponse;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesResponse;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsResponse;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsResponse;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.ActivitySessionDto;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollDashboardResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.BackofficeUserResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.EmotionalEventDto;
import com.huly.backend.infrastructure.presentation.dto.admin.PaymentEventDto;
import com.huly.backend.infrastructure.presentation.dto.admin.TopAppResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserActivitiesResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserAiDiagnosticsResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserAntiScrollResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserFinancialsResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.VectorMemoryDto;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivityMetric;
import com.huly.backend.domain.model.activity.ActivitiesKpiStats;
import com.huly.backend.domain.model.activity.ActivityPopularityStats;
import com.huly.backend.domain.model.activity.ActivityCorrelationStats;
import com.huly.backend.domain.model.activity.ActivityImpactStats;
import com.huly.backend.domain.dto.admin.activities.UpdateActivityConfigRequest;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivityResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminUpdateActivityConfigRequest;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivitiesKpiResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivityPopularityResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivityCorrelationResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminActivityImpactResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminPresentationMapper {

    public UserActivitiesResponse toUserActivitiesResponse(GetUserActivitiesResponse result) {
        List<ActivitySessionDto> sessions = result.activitySessions().stream()
                .map(s -> ActivitySessionDto.builder()
                        .id(s.id())
                        .activityType(s.activityType())
                        .createdAt(s.createdAt())
                        .build())
                .toList();

        return UserActivitiesResponse.builder()
                .activitySessions(sessions)
                .todayActivitiesCount(result.todayActivitiesCount())
                .favoriteActivity(result.favoriteActivity())
                .averageSessionsText(result.averageSessionsText())
                .activityDistribution(result.activityDistribution())
                .build();
    }

    public UserAiDiagnosticsResponse toUserAiDiagnosticsResponse(GetUserAiDiagnosticsResponse result) {
        List<VectorMemoryDto> aiMemories = result.aiMemories().stream()
                .map(m -> new VectorMemoryDto(m.id(), m.content(), m.sourceType(), m.contentType(), m.createdAt()))
                .toList();
        List<EmotionalEventDto> emotionalEvents = result.emotionalEvents().stream()
                .map(e -> EmotionalEventDto.builder()
                        .id(e.id())
                        .source(e.source())
                        .inputText(e.inputText())
                        .detectedEmotion(e.detectedEmotion())
                        .confidence(e.confidence())
                        .valence(e.valence())
                        .arousal(e.arousal())
                        .dominance(e.dominance())
                        .intensity(e.intensity())
                        .userGoal(e.userGoal())
                        .generatedRecommendation(e.generatedRecommendation())
                        .recommendedActivityId(e.recommendedActivityId())
                        .chosenActivityId(e.chosenActivityId())
                        .recommendationDecision(e.recommendationDecision())
                        .feedbackScore(e.feedbackScore())
                        .feedbackText(e.feedbackText())
                        .createdAt(e.createdAt())
                        .build())
                .toList();

        return UserAiDiagnosticsResponse.builder()
                .aiMemories(aiMemories)
                .emotionalEvents(emotionalEvents)
                .preferredName(result.preferredName())
                .communicationStyle(result.communicationStyle())
                .personalitySummary(result.personalitySummary())
                .topicsDetected(result.topicsDetected())
                .copingStrategies(result.copingStrategies())
                .receptivityScore(result.receptivityScore())
                .receptivityLabel(result.receptivityLabel())
                .acceptedActivities(result.acceptedActivities())
                .ignoredActivities(result.ignoredActivities())
                .dominantEmotion(result.dominantEmotion())
                .emotionDistribution(result.emotionDistribution())
                .build();
    }

    public UserFinancialsResponse toUserFinancialsResponse(GetUserFinancialsResponse result) {
        List<PaymentEventDto> events = result.paymentEvents().stream()
                .map(payment -> PaymentEventDto.builder()
                        .id(payment.id())
                        .productId(payment.productId())
                        .productName(payment.productName())
                        .productPrice(payment.productPrice())
                        .externalReference(payment.externalReference())
                        .mpPaymentId(payment.mpPaymentId())
                        .status(payment.status())
                        .coinsAmount(payment.coinsAmount())
                        .productType(payment.productType())
                        .createdAt(payment.createdAt())
                        .build())
                .toList();

        return UserFinancialsResponse.builder()
                .paymentEvents(events)
                .totalEarnings(result.totalEarnings())
                .build();
    }

    public UserAntiScrollResponse toUserAntiScrollResponse(GetUserAntiScrollStatsResponse result) {
        return UserAntiScrollResponse.builder()
                .antiScrollEnabled(result.antiScrollEnabled())
                .dataSharingConsent(result.dataSharingConsent())
                .mostUsedApp(result.mostUsedApp())
                .mostUsedAppActiveSeconds(result.mostUsedAppActiveSeconds())
                .totalScrollTimeSeconds(result.totalScrollTimeSeconds())
                .dailyScrollTimeSeconds(result.dailyScrollTimeSeconds())
                .topApps(result.topApps().stream().map(this::toTopAppResponse).toList())
                .build();
    }

    public BackofficeUserResponse toBackofficeUserResponse(BackofficeUserSummary user) {
        return BackofficeUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .birthDate(user.getBirthDate())
                .antiScrollEnabled(user.isAntiScrollEnabled())
                .dataSharingConsent(user.isDataSharingConsent())
                .mostUsedApp(user.getMostUsedApp())
                .mostUsedAppActiveSeconds(user.getMostUsedAppActiveSeconds())
                .totalScrollTimeSeconds(user.getTotalScrollTimeSeconds())
                .dailyScrollTimeSeconds(user.getDailyScrollTimeSeconds())
                .topApps(user.getTopApps() != null ? user.getTopApps().stream().map(this::toTopAppResponse).toList() : List.of())
                .coins(user.getCoins())
                .plan(user.getPlan())
                .dominantEmotion(user.getDominantEmotion())
                .build();
    }

    public AntiScrollDashboardResponse toAntiScrollDashboardResponse(AntiScrollDashboardStats stats) {
        return AntiScrollDashboardResponse.builder()
                .totalModalsShown(stats.getTotalModalsShown())
                .totalRedirects(stats.getTotalRedirects())
                .totalUsersCount(stats.getTotalUsersCount())
                .activeExtensionUsersCount(stats.getActiveExtensionUsersCount())
                .dataSharingConsentUsersCount(stats.getDataSharingConsentUsersCount())
                .topUsedApps(stats.getTopUsedApps().stream().map(this::toTopAppResponse).toList())
                .build();
    }

    public AntiScrollConfigResponse toAntiScrollConfigResponse(GetAntiScrollGlobalConfigResponse config) {
        return AntiScrollConfigResponse.builder()
                .defaultPauseIntervalMinutes(config.defaultPauseIntervalMinutes())
                .termsAndConditions(config.termsAndConditions())
                .build();
    }

    private TopAppResponse toTopAppResponse(TopAppStats app) {
        return new TopAppResponse(app.getDomain(), app.getTotalActiveSeconds());
    }

    public AdminActivityResponse toResponse(Activity activity) {
        return new AdminActivityResponse(
                activity.getId(),
                activity.getType(),
                activity.getValenceMin(),
                activity.getValenceMax(),
                activity.getArousalMin(),
                activity.getArousalMax(),
                activity.getDominanceMin(),
                activity.getDominanceMax(),
                activity.getEffectValence(),
                activity.getEffectArousal(),
                activity.getEffectDominance(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getGoalKeywords(),
                activity.getRoutePath()
        );
    }

    public UpdateActivityConfigRequest toDomainRequest(AdminUpdateActivityConfigRequest request) {
        return new UpdateActivityConfigRequest(
                request.getValenceMin(),
                request.getValenceMax(),
                request.getArousalMin(),
                request.getArousalMax(),
                request.getDominanceMin(),
                request.getDominanceMax(),
                request.getEffectValence(),
                request.getEffectArousal(),
                request.getEffectDominance(),
                request.getTitle(),
                request.getDescription(),
                request.getGoalKeywords(),
                request.getRoutePath()
        );
    }

    public AdminActivitiesKpiResponse toKpiResponse(ActivitiesKpiStats stats) {
        return new AdminActivitiesKpiResponse(
                stats.getTotalSessions(),
                new AdminActivitiesKpiResponse.TopActivity(
                        stats.getTopActivityType(),
                        stats.getTopActivitySessions()
                ),
                stats.getAverageMoodImprovement()
        );
    }

    public AdminActivityPopularityResponse toPopularityResponse(ActivityPopularityStats stats) {
        return new AdminActivityPopularityResponse(
                stats.getActivityType(),
                stats.getActivityName(),
                stats.getTotalSessions()
        );
    }

    public AdminActivityCorrelationResponse toCorrelationResponse(ActivityCorrelationStats stats) {
        return new AdminActivityCorrelationResponse(
                stats.getActivityType(),
                stats.getEmotion(),
                stats.getSuggestionsCount(),
                stats.getAcceptanceRate()
        );
    }

    public AdminActivityImpactResponse toImpactResponse(ActivityImpactStats stats) {
        return new AdminActivityImpactResponse(
                stats.getActivityType(),
                stats.getAverageValenceChange(),
                stats.getAverageArousalChange(),
                stats.isBasedOnMetrics()
        );
    }
}
