package com.huly.backend.domain.model.chat;

import jakarta.validation.constraints.NotBlank;

public record UpdateBotConfigCommand(

        Boolean riskDetectionEnabled,

        @NotBlank
        String systemPrompt,

        Boolean preferredNameQuestionEnabled,

        Boolean communicationStyleQuestionEnabled
) {
    public UpdateBotConfigCommand(Boolean riskDetectionEnabled, String systemPrompt) {
        this(riskDetectionEnabled, systemPrompt, null, null);
    }
}
