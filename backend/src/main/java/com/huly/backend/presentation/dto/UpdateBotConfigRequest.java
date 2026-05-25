package com.huly.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBotConfigRequest(
        Boolean riskDetectionEnabled,
        @NotBlank String systemPrompt
) {
}