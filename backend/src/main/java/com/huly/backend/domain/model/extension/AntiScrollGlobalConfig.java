package com.huly.backend.domain.model.extension;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class AntiScrollGlobalConfig {
    private final Long id;
    private final Integer defaultPauseIntervalMinutes;
    private final String termsAndConditions;
}
