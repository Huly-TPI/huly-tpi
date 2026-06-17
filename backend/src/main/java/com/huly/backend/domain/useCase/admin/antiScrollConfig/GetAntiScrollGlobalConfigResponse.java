package com.huly.backend.domain.useCase.admin.antiScrollConfig;

public record GetAntiScrollGlobalConfigResponse(
        int defaultPauseIntervalMinutes,
        String termsAndConditions
) {
}
