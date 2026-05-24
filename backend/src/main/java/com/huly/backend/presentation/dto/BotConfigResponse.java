package com.huly.backend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BotConfigResponse(
        Long id,
        @JsonProperty("risk_detection_enabled") Boolean riskDetectionEnabled,
        @JsonProperty("system_prompt") String systemPrompt
) {
}