package com.huly.backend.domain.useCase.admin.antiScrollConfig;

public record UpdateAntiScrollConfigRequest(
        int defaultPauseIntervalMinutes,
        String termsAndConditions
) {
}
