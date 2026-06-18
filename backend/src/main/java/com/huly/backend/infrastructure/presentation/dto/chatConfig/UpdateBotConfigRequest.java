package com.huly.backend.infrastructure.presentation.dto.chatConfig;

import jakarta.validation.constraints.NotBlank;

public record UpdateBotConfigRequest(
        Boolean riskDetectionEnabled,
        @NotBlank String systemPrompt,
        Boolean preferredNameQuestionEnabled,
        Boolean communicationStyleQuestionEnabled
) {
    public UpdateBotConfigRequest(Boolean riskDetectionEnabled, String systemPrompt) {
        this(riskDetectionEnabled, systemPrompt, null, null);
    }
}
