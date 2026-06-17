package com.huly.backend.domain.useCase.admin.antiScrollConfig;

public record GetAntiScrollConfigResponse(
        int defaultPauseIntervalMinutes,
        String termsAndConditions
) {
}
