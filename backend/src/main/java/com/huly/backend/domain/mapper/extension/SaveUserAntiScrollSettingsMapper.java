package com.huly.backend.domain.mapper.extension;

import com.huly.backend.domain.dto.extension.SaveUserAntiScrollSettingsRequest;
import com.huly.backend.domain.dto.extension.SaveUserAntiScrollSettingsResponse;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;

/**
 * Mapper de dominio para el caso de uso de guardado de configuracion anti-scroll.
 */
public class SaveUserAntiScrollSettingsMapper {

    public UserAntiScrollSettings toModel(SaveUserAntiScrollSettingsRequest request) {
        return UserAntiScrollSettings.builder()
                .enabled(request.enabled())
                .pauseIntervalSeconds(request.pauseIntervalSeconds())
                .monitoredDomains(request.monitoredDomains())
                .dataSharingConsent(request.dataSharingConsent())
                .build();
    }

    public SaveUserAntiScrollSettingsResponse toResponse() {
        return new SaveUserAntiScrollSettingsResponse();
    }
}
