package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatPreferenceDetectionResultTest {

    @Test
    @DisplayName("Descarta la extracción por debajo del umbral de confianza")
    void sanitizedShouldDiscardLowConfidence() {
        ChatPreferenceDetectionResult result =
                result("Sergito", null, ChatPreferenceMessageType.PREFERENCE_ONLY, 0.5).sanitized();

        assertThat(result.hasPreference()).isFalse();
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.UNRELATED);
    }

    @Test
    @DisplayName("Sanitiza el nombre cuando la confianza es suficiente")
    void sanitizedShouldSanitizeNameWhenConfident() {
        ChatPreferenceDetectionResult result =
                result("sergito", null, ChatPreferenceMessageType.PREFERENCE_ONLY, 0.9).sanitized();

        assertThat(result.preferredName()).isEqualTo("Sergito");
        assertThat(result.communicationStyle()).isNull();
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.PREFERENCE_ONLY);
    }

    @Test
    @DisplayName("Acepta el umbral exacto de confianza")
    void sanitizedShouldAcceptExactThreshold() {
        ChatPreferenceDetectionResult result =
                result("sergito", null, ChatPreferenceMessageType.PREFERENCE_ONLY, 0.85).sanitized();

        assertThat(result.preferredName()).isEqualTo("Sergito");
    }

    @Test
    @DisplayName("Devuelve unrelated cuando el nombre es inválido y no hay estilo")
    void sanitizedShouldReturnUnrelatedWhenNameInvalidAndNoStyle() {
        ChatPreferenceDetectionResult result =
                result("hola", null, ChatPreferenceMessageType.PREFERENCE_ONLY, 0.9).sanitized();

        assertThat(result.hasPreference()).isFalse();
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.UNRELATED);
    }

    @Test
    @DisplayName("Conserva el estilo aunque el nombre sea inválido")
    void sanitizedShouldKeepStyleWhenNameInvalid() {
        ChatPreferenceDetectionResult result =
                result("hola", CommunicationStyle.INFORMAL, ChatPreferenceMessageType.MIXED, 0.9).sanitized();

        assertThat(result.preferredName()).isNull();
        assertThat(result.communicationStyle()).isEqualTo(CommunicationStyle.INFORMAL);
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.MIXED);
    }

    @Test
    @DisplayName("Usa MIXED cuando el tipo de mensaje es nulo")
    void sanitizedShouldDefaultToMixedWhenTypeIsNull() {
        ChatPreferenceDetectionResult result =
                result("sergito", null, null, 0.9).sanitized();

        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.MIXED);
    }

    private ChatPreferenceDetectionResult result(
            String name,
            CommunicationStyle style,
            ChatPreferenceMessageType type,
            double confidence) {
        return new ChatPreferenceDetectionResult(name, style, type, confidence);
    }
}
