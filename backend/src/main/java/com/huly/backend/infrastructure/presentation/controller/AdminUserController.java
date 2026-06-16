package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesRequest;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesResponse;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsRequest;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsResponse;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsUseCase;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsRequest;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsResponse;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsUseCase;
import com.huly.backend.infrastructure.presentation.dto.admin.ActivitySessionDto;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollDashboardResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.BackofficeUserResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.EmotionalEventDto;
import com.huly.backend.infrastructure.presentation.dto.admin.PaymentEventDto;
import com.huly.backend.infrastructure.presentation.dto.admin.TopAppResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.VectorMemoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huly.backend.domain.repository.extension.AntiScrollConfigRepository;
import com.huly.backend.domain.model.extension.AntiScrollConfig;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.huly.backend.infrastructure.presentation.dto.admin.EmotionalEventDto;
import com.huly.backend.infrastructure.presentation.dto.admin.PaymentEventDto;
import com.huly.backend.infrastructure.presentation.dto.admin.TopAppResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.VectorMemoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huly.backend.domain.repository.extension.AntiScrollConfigRepository;
import com.huly.backend.domain.model.extension.AntiScrollConfig;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import com.huly.backend.infrastructure.presentation.dto.admin.UserActivitiesResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserAiDiagnosticsResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserFinancialsResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.UserAntiScrollResponse;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsUseCase;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsRequest;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final ListBackofficeUsersUseCase listBackofficeUsersUseCase;
    private final GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase;
    private final AntiScrollConfigRepository antiScrollConfigRepository;
    private final GetUserActivitiesUseCase getUserActivitiesUseCase;
    private final GetUserAiDiagnosticsUseCase getUserAiDiagnosticsUseCase;
    private final GetUserFinancialsUseCase getUserFinancialsUseCase;
    private final GetUserAntiScrollStatsUseCase getUserAntiScrollStatsUseCase;

    @GetMapping("/{id}/statistics/activities")
    public ResponseEntity<UserActivitiesResponse> getUserActivities(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "total") String timeframe) {
        GetUserActivitiesResponse result = getUserActivitiesUseCase.execute(new GetUserActivitiesRequest(id, timeframe));
        List<ActivitySessionDto> sessions = result.activitySessions().stream()
                .map(s -> ActivitySessionDto.builder()
                        .id(s.id())
                        .activityType(s.activityType())
                        .createdAt(s.createdAt())
                        .build())
                .toList();
        UserActivitiesResponse response = UserActivitiesResponse.builder()
                .activitySessions(sessions)
                .todayActivitiesCount(result.todayActivitiesCount())
                .favoriteActivity(result.favoriteActivity())
                .averageSessionsText(result.averageSessionsText())
                .activityDistribution(result.activityDistribution())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statistics/ai")
    public ResponseEntity<UserAiDiagnosticsResponse> getUserAiDiagnostics(@PathVariable Long id) {
        GetUserAiDiagnosticsResponse result = getUserAiDiagnosticsUseCase.execute(new GetUserAiDiagnosticsRequest(id));
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
        UserAiDiagnosticsResponse response = UserAiDiagnosticsResponse.builder()
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
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statistics/finance")
    public ResponseEntity<UserFinancialsResponse> getUserFinancials(@PathVariable Long id) {
        GetUserFinancialsResponse result = getUserFinancialsUseCase.execute(new GetUserFinancialsRequest(id));
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

        UserFinancialsResponse response = UserFinancialsResponse.builder()
                .paymentEvents(events)
                .totalEarnings(result.totalEarnings())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statistics/antiscroll")
    public ResponseEntity<UserAntiScrollResponse> getUserAntiScrollStats(@PathVariable Long id) {
        GetUserAntiScrollStatsResponse result = getUserAntiScrollStatsUseCase.execute(new GetUserAntiScrollStatsRequest(id));
        List<TopAppResponse> topApps = result.topApps().stream()
                .map(app -> new TopAppResponse(app.getDomain(), app.getTotalActiveSeconds()))
                .toList();

        UserAntiScrollResponse response = UserAntiScrollResponse.builder()
                .antiScrollEnabled(result.antiScrollEnabled())
                .dataSharingConsent(result.dataSharingConsent())
                .mostUsedApp(result.mostUsedApp())
                .mostUsedAppActiveSeconds(result.mostUsedAppActiveSeconds())
                .totalScrollTimeSeconds(result.totalScrollTimeSeconds())
                .dailyScrollTimeSeconds(result.dailyScrollTimeSeconds())
                .topApps(topApps)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BackofficeUserResponse>> getBackofficeUsers() {
        List<BackofficeUserResponse> responses = listBackofficeUsersUseCase.execute().stream()
                .map(u -> BackofficeUserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .role(u.getRole() != null ? u.getRole().name() : null)
                        .status(u.getStatus() != null ? u.getStatus().name() : null)
                        .birthDate(u.getBirthDate())
                        .antiScrollEnabled(u.isAntiScrollEnabled())
                        .dataSharingConsent(u.isDataSharingConsent())
                        .mostUsedApp(u.getMostUsedApp())
                        .mostUsedAppActiveSeconds(u.getMostUsedAppActiveSeconds())
                        .totalScrollTimeSeconds(u.getTotalScrollTimeSeconds())
                        .dailyScrollTimeSeconds(u.getDailyScrollTimeSeconds())
                        .topApps(u.getTopApps() != null ? u.getTopApps().stream()
                                .map(t -> new TopAppResponse(t.getDomain(), t.getTotalActiveSeconds()))
                                .toList() : List.of())
                        .coins(u.getCoins())
                        .plan(u.getPlan())
                        .build())
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/antiscroll/dashboard")
    public ResponseEntity<AntiScrollDashboardResponse> getDashboardStats() {
        var stats = getAntiScrollDashboardUseCase.execute();
        List<TopAppResponse> topApps = stats.getTopUsedApps().stream()
                .map(t -> new TopAppResponse(t.getDomain(), t.getTotalActiveSeconds()))
                .toList();

        return ResponseEntity.ok(AntiScrollDashboardResponse.builder()
                .totalModalsShown(stats.getTotalModalsShown())
                .totalRedirects(stats.getTotalRedirects())
                .totalUsersCount(stats.getTotalUsersCount())
                .activeExtensionUsersCount(stats.getActiveExtensionUsersCount())
                .dataSharingConsentUsersCount(stats.getDataSharingConsentUsersCount())
                .topUsedApps(topApps)
                .build());
    }

    @GetMapping("/antiscroll/config")
    public ResponseEntity<AntiScrollConfigResponse> getAntiScrollConfig() {
        AntiScrollConfig config = antiScrollConfigRepository.findFirst()
                .orElse(AntiScrollConfig.builder()
                        .defaultPauseIntervalMinutes(20)
                        .termsAndConditions("El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentración o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. ¡Cero presiones, el ritmo lo marcás vos!")
                        .build());
        return ResponseEntity.ok(AntiScrollConfigResponse.builder()
                .defaultPauseIntervalMinutes(config.getDefaultPauseIntervalMinutes())
                .termsAndConditions(config.getTermsAndConditions())
                .build());
    }

    @PostMapping("/antiscroll/config")
    public ResponseEntity<Void> updateAntiScrollConfig(@Valid @RequestBody AntiScrollConfigRequest request) {
        AntiScrollConfig existing = antiScrollConfigRepository.findFirst()
                .orElse(AntiScrollConfig.builder().build());

        AntiScrollConfig updated = AntiScrollConfig.builder()
                .id(existing.getId())
                .defaultPauseIntervalMinutes(request.getDefaultPauseIntervalMinutes())
                .termsAndConditions(request.getTermsAndConditions())
                .build();

        antiScrollConfigRepository.save(updated);
        return ResponseEntity.ok().build();
    }
}
