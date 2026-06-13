package com.huly.backend.domain.model.extension;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class AntiScrollConfig {
    private final Long id;
    private final Integer defaultPauseIntervalMinutes;
    private final String termsAndConditions;
}
