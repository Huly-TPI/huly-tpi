package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.useCase.extension.GetUserAntiScrollSettingsResponse;
import com.huly.backend.domain.useCase.extension.GetUserAntiScrollSettingsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionMetricsUseCase;
import com.huly.backend.domain.useCase.extension.SaveUserAntiScrollSettingsUseCase;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionMetricRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.AntiScrollSettingsRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.AntiScrollSettingsResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extension")
@RequiredArgsConstructor
public class ExtensionController {

    private final GetUserAntiScrollSettingsUseCase getUserAntiScrollSettingsUseCase;
    private final SaveUserAntiScrollSettingsUseCase saveUserAntiScrollSettingsUseCase;
    private final SaveExtensionMetricsUseCase saveExtensionMetricsUseCase;

    @GetMapping("/settings")
    public ResponseEntity<AntiScrollSettingsResponse> getSettings(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = getUserId(principal);
        GetUserAntiScrollSettingsResponse settings = getUserAntiScrollSettingsUseCase.execute(userId);
        return ResponseEntity.ok(toResponse(settings));
    }

    @PostMapping("/settings")
    public ResponseEntity<Void> saveSettings(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AntiScrollSettingsRequest request
    ) {
        Long userId = getUserId(principal);
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(request.isEnabled())
                .pauseIntervalSeconds(request.getPauseIntervalSeconds())
                .monitoredDomains(request.getMonitoredDomains())
                .dataSharingConsent(request.isDataSharingConsent())
                .build();
        saveUserAntiScrollSettingsUseCase.execute(userId, settings);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics")
    public ResponseEntity<Void> saveMetrics(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody List<ExtensionMetricRequest> requests
    ) {
        Long userId = getUserId(principal);
        List<ExtensionMetric> metrics = requests.stream()
                .map(this::toDomain)
                .toList();
        saveExtensionMetricsUseCase.execute(userId, metrics);
        return ResponseEntity.ok().build();
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) 
            throw new UnauthorizedException("Not authenticated");       

        return Long.parseLong(principal.getUsername());
    }

    private AntiScrollSettingsResponse toResponse(GetUserAntiScrollSettingsResponse settings) {
        return AntiScrollSettingsResponse.builder()
                .enabled(settings.enabled())
                .pauseIntervalSeconds(settings.pauseIntervalSeconds())
                .gardenUrl(settings.gardenUrl())
                .backendUrl(settings.backendUrl())
                .monitoredDomains(settings.monitoredDomains())
                .dataSharingConsent(settings.dataSharingConsent())
                .userName(settings.userName())
                .termsAndConditions(settings.termsAndConditions())
                .build();
    }

    private ExtensionMetric toDomain(ExtensionMetricRequest request) {
        return ExtensionMetric.builder()
                .domain(request.getDomain())
                .activeSeconds(request.getActiveSeconds())
                .scrollCount(request.getScrollCount())
                .modalsShown(request.getModalsShown())
                .redirects(request.getRedirects())
                .build();
    }
}
