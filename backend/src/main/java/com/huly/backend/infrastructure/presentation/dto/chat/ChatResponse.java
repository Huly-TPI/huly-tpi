package com.huly.backend.infrastructure.presentation.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({})
public record ChatResponse(
        @JsonProperty("huly_reply")
        String hulyReply,

        @JsonProperty("detected_emotion")
        String detectedEmotion,

        Integer intensity,

        @JsonProperty("suggested_action")
        SuggestedAction suggestedAction,

        @JsonProperty("generated_challenge")
        GeneratedChallenge generatedChallenge,

        Metadata metadata,

        @JsonProperty("remaining_messages")
        Integer remainingMessages,

        @JsonProperty("limit_message")
        String limitMessage,

        @JsonProperty("remaining_audio_messages")
        Integer remainingAudioMessages,

        @JsonProperty("audio_limit_message")
        String audioLimitMessage
) {

        public record SuggestedAction(
                String type,
                @JsonProperty("action_id") String actionId,
                String title,
                String description,
                @JsonProperty("action_url") String actionUrl,
                @JsonProperty("emotional_event_id") Long emotionalEventId
        ) {
                public SuggestedAction(
                        String type,
                        String actionId,
                        String title,
                        String description,
                        String actionUrl
                ) {
                        this(type, actionId, title, description, actionUrl, null);
                }
        }

        public record GeneratedChallenge(String title, String description) {}

        public record Metadata(
                @JsonProperty("risk_detected") Boolean riskDetected,
                @JsonProperty("matched_word") String matchedWord
        ) {}
}
