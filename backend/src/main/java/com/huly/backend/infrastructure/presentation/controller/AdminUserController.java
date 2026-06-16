package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollDashboardResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.BackofficeUserResponse;
import com.huly.backend.infrastructure.presentation.dto.admin.TopAppResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final ListBackofficeUsersUseCase listBackofficeUsersUseCase;
    private final GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase;
    private final AntiScrollConfigRepository antiScrollConfigRepository;

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
