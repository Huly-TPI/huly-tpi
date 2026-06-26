package com.huly.backend.domain.dto.extension;

import lombok.Builder;

import java.util.List;

/**
 * Respuesta de dominio con la configuracion anti-scroll de un usuario.
 */
@Builder
public record GetUserAntiScrollSettingsResponse(
        boolean enabled,
        int pauseIntervalSeconds,
        String gardenUrl,
        String backendUrl,
        List<String> monitoredDomains,
        boolean dataSharingConsent,
        String userName,
        String termsAndConditions
) {
}
