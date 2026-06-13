package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AntiScrollConfigResponse {
    private int defaultPauseIntervalMinutes;
    private String termsAndConditions;
}
