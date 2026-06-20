package com.huly.backend.infrastructure.presentation.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ChatMessageResponse(
        Long id,
        String role,
        String content,
        @JsonProperty("risk_detected") Boolean riskDetected,
        @JsonProperty("detected_emotion") String detectedEmotion,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("suggested_action") ChatResponse.SuggestedAction suggestedAction,
        @JsonProperty("generated_challenge") ChatResponse.GeneratedChallenge generatedChallenge,
        @JsonProperty("suggested_action_decision") String suggestedActionDecision,
        @JsonProperty("challenge_decision") String challengeDecision
) {}
