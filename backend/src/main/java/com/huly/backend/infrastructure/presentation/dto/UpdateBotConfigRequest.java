package com.huly.backend.infrastructure.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBotConfigRequest(
        Boolean riskDetectionEnabled,
        @NotBlank String systemPrompt
) {
}