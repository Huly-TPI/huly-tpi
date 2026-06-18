package com.huly.backend.infrastructure.presentation.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AntiScrollConfigRequest {
    @Min(1)
    private int defaultPauseIntervalMinutes;

    @NotBlank
    private String termsAndConditions;
}
