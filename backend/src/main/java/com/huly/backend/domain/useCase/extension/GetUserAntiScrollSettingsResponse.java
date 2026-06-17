package com.huly.backend.domain.useCase.extension;

import lombok.Builder;

import java.util.List;

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
