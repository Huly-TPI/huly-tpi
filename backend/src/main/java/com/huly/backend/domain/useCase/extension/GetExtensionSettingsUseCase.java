package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.UserProfile;
import com.huly.backend.domain.model.extension.AntiScrollConfig;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.extension.AntiScrollConfigRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetExtensionSettingsUseCase {
    private static final List<String> DEFAULT_MONITORED_DOMAINS =
            List.of("twitter.com", "x.com", "instagram.com", "tiktok.com", "youtube.com", "facebook.com");

    private static final String DEFAULT_TERMS_AND_CONDITIONS =
            "El modo anti-scroll es simplemente una herramienta para acompa\u00f1arte cuando sientas que necesit\u00e1s frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentraci\u00f3n o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. \u00a1Cero presiones, el ritmo lo marc\u00e1s vos!";

    private final ExtensionSettingsRepository settingsRepository;
    private final AntiScrollConfigRepository antiScrollConfigRepository;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final String frontendUrl;
    private final String backendUrl;

    public GetExtensionSettingsResponse execute(Long userId) {
        AntiScrollConfig config = antiScrollConfigRepository.findFirst().orElse(null);
        int defaultIntervalSeconds = (config != null ? config.getDefaultPauseIntervalMinutes() : 20) * 60;
        String termsAndConditions = config != null ? config.getTermsAndConditions() : DEFAULT_TERMS_AND_CONDITIONS;

        ExtensionSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> ExtensionSettings.builder()
                        .enabled(true)
                        .pauseIntervalSeconds(defaultIntervalSeconds)
                        .monitoredDomains(DEFAULT_MONITORED_DOMAINS)
                        .dataSharingConsent(false)
                        .build());

        UserProfile profile = getCurrentUserUseCase.execute(userId);
        List<String> monitoredDomains = settings.getMonitoredDomains() == null || settings.getMonitoredDomains().isEmpty()
                ? DEFAULT_MONITORED_DOMAINS
                : settings.getMonitoredDomains();

        return GetExtensionSettingsResponse.builder()
                .enabled(settings.isEnabled())
                .pauseIntervalSeconds(settings.getPauseIntervalSeconds())
                .gardenUrl(frontendUrl + "/")
                .backendUrl(backendUrl)
                .monitoredDomains(monitoredDomains)
                .dataSharingConsent(settings.isDataSharingConsent())
                .userName(profile.user().getName())
                .termsAndConditions(termsAndConditions)
                .build();
    }
}
