package com.huly.backend.domain.mapper.extension;

import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsResponse;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de obtencion de configuracion anti-scroll.
 */
public class GetUserAntiScrollSettingsMapper {

    public GetUserAntiScrollSettingsResponse toResponse(
            UserAntiScrollSettings settings,
            List<String> monitoredDomains,
            String gardenUrl,
            String backendUrl,
            String userName,
            String termsAndConditions
    ) {
        return GetUserAntiScrollSettingsResponse.builder()
                .enabled(settings.isEnabled())
                .pauseIntervalSeconds(settings.getPauseIntervalSeconds())
                .gardenUrl(gardenUrl)
                .backendUrl(backendUrl)
                .monitoredDomains(monitoredDomains)
                .dataSharingConsent(settings.isDataSharingConsent())
                .userName(userName)
                .termsAndConditions(termsAndConditions)
                .build();
    }
}
