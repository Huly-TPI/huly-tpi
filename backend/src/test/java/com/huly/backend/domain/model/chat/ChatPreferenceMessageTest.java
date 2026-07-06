package com.huly.backend.domain.model.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatPreferenceMessageTest {

    @Test
    @DisplayName("Detecta una señal de cambio de nombre")
    void hasSignalShouldDetectNameSignal() {
        assertThat(hasSignal("decime Checho")).isTrue();
        assertThat(hasSignal("cambia mi nombre a Pedro")).isTrue();
    }

    @Test
    @DisplayName("Detecta una señal de cambio de estilo")
    void hasSignalShouldDetectStyleSignal() {
        assertThat(hasSignal("hablame mas informal")).isTrue();
        assertThat(hasSignal("cambia el tono")).isTrue();
    }

    @Test
    @DisplayName("Ignora acentos al detectar la señal")
    void hasSignalShouldIgnoreAccents() {
        assertThat(hasSignal("Llámame Pedro")).isTrue();
    }

    @Test
    @DisplayName("Es insensible a mayúsculas")
    void hasSignalShouldBeCaseInsensitive() {
        assertThat(hasSignal("DECIME algo")).isTrue();
    }

    @Test
    @DisplayName("No detecta señal en un mensaje sin intención de cambio")
    void hasSignalShouldReturnFalseWithoutSignal() {
        assertThat(hasSignal("estoy muy triste hoy")).isFalse();
    }

    @Test
    @DisplayName("No detecta señal en null ni en cadenas en blanco")
    void hasSignalShouldReturnFalseForNullAndBlank() {
        assertThat(hasSignal(null)).isFalse();
        assertThat(hasSignal("   ")).isFalse();
    }

    private boolean hasSignal(String message) {
        return ChatPreferenceMessage.of(message).hasPreferenceChangeSignal();
    }
}
