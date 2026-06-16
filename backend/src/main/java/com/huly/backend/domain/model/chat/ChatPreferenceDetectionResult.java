package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;

/**
 * Structured conversational preference extraction result.
 */
public record ChatPreferenceDetectionResult(
        String preferredName,
        CommunicationStyle communicationStyle,
        ChatPreferenceMessageType messageType,
        double confidence
) {
    public static ChatPreferenceDetectionResult unrelated() {
        return new ChatPreferenceDetectionResult(
                null,
                null,
                ChatPreferenceMessageType.UNRELATED,
                0.0);
    }

    public boolean hasPreference() {
        return preferredName != null || communicationStyle != null;
    }
}
