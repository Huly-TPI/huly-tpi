package com.huly.backend.domain.model;

import jakarta.validation.constraints.NotBlank;

public record UpdateBotConfigCommand(

        Boolean riskDetectionEnabled,

        @NotBlank
        String systemPrompt
) {
}