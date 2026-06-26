package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsRequest;
import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsResponse;
import com.huly.backend.domain.mapper.extension.GetUserAntiScrollSettingsMapper;
import com.huly.backend.domain.model.user.UserProfile;
import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetUserAntiScrollSettingsUseCase {
    private static final List<String> DEFAULT_MONITORED_DOMAINS =
            List.of("twitter.com", "x.com", "instagram.com", "tiktok.com", "youtube.com", "facebook.com");

    private static final String DEFAULT_TERMS_AND_CONDITIONS =
            "El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentración o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. ¡Cero presiones, el ritmo lo marcás vos!";

    private final UserAntiScrollSettingsRepository settingsRepository;
    private final AntiScrollGlobalConfigRepository antiScrollConfigRepository;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final String frontendUrl;
    private final String backendUrl;
    private final GetUserAntiScrollSettingsMapper mapper;

    public GetUserAntiScrollSettingsResponse execute(GetUserAntiScrollSettingsRequest request) {
        Long userId = request.userId();
        AntiScrollGlobalConfig config = antiScrollConfigRepository.findFirst().orElse(null);
        int defaultIntervalSeconds = (config != null ? config.getDefaultPauseIntervalMinutes() : 20) * 60;
        String termsAndConditions = config != null ? config.getTermsAndConditions() : DEFAULT_TERMS_AND_CONDITIONS;

        UserAntiScrollSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> UserAntiScrollSettings.builder()
                        .enabled(true)
                        .pauseIntervalSeconds(defaultIntervalSeconds)
                        .monitoredDomains(DEFAULT_MONITORED_DOMAINS)
                        .dataSharingConsent(false)
                        .build());

        UserProfile profile = getCurrentUserUseCase.execute(userId);
        List<String> monitoredDomains = settings.getMonitoredDomains() == null || settings.getMonitoredDomains().isEmpty()
                ? DEFAULT_MONITORED_DOMAINS
                : settings.getMonitoredDomains();

        return mapper.toResponse(
                settings,
                monitoredDomains,
                frontendUrl + "/",
                backendUrl,
                profile.user().getName(),
                termsAndConditions
        );
    }
}
