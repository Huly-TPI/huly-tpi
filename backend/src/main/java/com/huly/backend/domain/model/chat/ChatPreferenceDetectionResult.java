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
    private static final double MIN_CONFIDENCE = 0.85;

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

    /**
     * Valida esta extracción semántica: descarta resultados por debajo del umbral de
     * confianza, sanitiza el nombre con las reglas de {@link PreferredName} y devuelve
     * {@link #unrelated()} si no queda ninguna preferencia usable.
     */
    public ChatPreferenceDetectionResult sanitized() {
        if (confidence < MIN_CONFIDENCE) {
            return unrelated();
        }

        String validName = PreferredName.sanitize(preferredName).orElse(null);
        if (validName == null && communicationStyle == null) {
            return unrelated();
        }

        ChatPreferenceMessageType resolvedType = messageType != null
                ? messageType
                : ChatPreferenceMessageType.MIXED;
        return new ChatPreferenceDetectionResult(validName, communicationStyle, resolvedType, confidence);
    }
}
