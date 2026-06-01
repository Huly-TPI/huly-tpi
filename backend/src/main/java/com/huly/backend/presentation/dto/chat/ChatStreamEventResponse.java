package com.huly.backend.presentation.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatStreamEventResponse(
        String type,
        String delta,

        @JsonProperty("huly_reply")
        String hulyReply,

        @JsonProperty("detected_emotion")
        String detectedEmotion,

        Integer intensity,
        ChatResponse.Metadata metadata,
        String error
) {
}
