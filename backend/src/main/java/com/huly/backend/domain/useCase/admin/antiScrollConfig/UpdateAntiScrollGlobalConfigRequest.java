package com.huly.backend.domain.useCase.admin.antiScrollConfig;

public record UpdateAntiScrollGlobalConfigRequest(
        int defaultPauseIntervalMinutes,
        String termsAndConditions
) {
}
