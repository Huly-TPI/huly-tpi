package com.huly.backend.infrastructure.presentation.dto.chatConfig;

public record BotConfigResponse(
        Long id,
        Boolean riskDetectionEnabled,
        String systemPrompt,
        Boolean preferredNameQuestionEnabled,
        Boolean communicationStyleQuestionEnabled
) {
}
