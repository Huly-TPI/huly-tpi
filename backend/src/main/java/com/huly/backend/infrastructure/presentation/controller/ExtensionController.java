package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.useCase.extension.GetExtensionSettingsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionMetricsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionSettingsUseCase;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionMetricRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionSettingsRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionSettingsResponse;
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

    private final GetExtensionSettingsUseCase getExtensionSettingsUseCase;
    private final SaveExtensionSettingsUseCase saveExtensionSettingsUseCase;
    private final SaveExtensionMetricsUseCase saveExtensionMetricsUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final com.huly.backend.domain.repository.extension.AntiScrollConfigRepository antiScrollConfigRepository;

    @GetMapping("/settings")
    public ResponseEntity<ExtensionSettingsResponse> getSettings(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = getUserId(principal);
        ExtensionSettings settings = getExtensionSettingsUseCase.execute(userId);
        String userName = getCurrentUserUseCase.execute(userId).user().getName();
        String terms = antiScrollConfigRepository.findFirst()
                .map(com.huly.backend.domain.model.extension.AntiScrollConfig::getTermsAndConditions)
                .orElse("El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentración o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. ¡Cero presiones, el ritmo lo marcás vos!");
        return ResponseEntity.ok(toResponse(settings, userName, terms));
    }

    @PostMapping("/settings")
    public ResponseEntity<Void> saveSettings(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ExtensionSettingsRequest request
    ) {
        Long userId = getUserId(principal);
        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(request.isEnabled())
                .pauseIntervalSeconds(request.getPauseIntervalSeconds())
                .monitoredDomains(request.getMonitoredDomains())
                .dataSharingConsent(request.isDataSharingConsent())
                .build();
        saveExtensionSettingsUseCase.execute(userId, settings);
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

    private ExtensionSettingsResponse toResponse(ExtensionSettings settings, String userName, String termsAndConditions) {
        return ExtensionSettingsResponse.builder()
                .enabled(settings.isEnabled())
                .pauseIntervalSeconds(settings.getPauseIntervalSeconds())
                .gardenUrl(settings.getGardenUrl())
                .backendUrl(settings.getBackendUrl())
                .monitoredDomains(settings.getMonitoredDomains())
                .dataSharingConsent(settings.isDataSharingConsent())
                .userName(userName)
                .termsAndConditions(termsAndConditions)
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
